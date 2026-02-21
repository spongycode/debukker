package com.spongycode.debukker.network

import com.spongycode.debukker.DebugConfigManager
import com.spongycode.debukker.models.NetworkRequest
import com.spongycode.debukker.models.NetworkResponse
import com.spongycode.debukker.models.NetworkTransaction
import com.spongycode.debukker.models.ResponseMock
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpResponseData
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val DebugStartTimeKey = AttributeKey<Long>("DebugStartTime")
private val MockedResponseKey = AttributeKey<ResponseMock>("MockedResponse")
private val TransactionIdKey = AttributeKey<String>("TransactionId")

@OptIn(ExperimentalUuidApi::class, InternalAPI::class)
val DebugNetworkPlugin = createClientPlugin("DebugNetworkPlugin") {

    client.plugin(HttpSend).intercept { request ->
        val config = DebugConfigManager.config.value
        val url = request.url.toString()
        val method = request.method.value

        if (config.isEnabled && config.isResponseMockingEnabled) {
            val responseMock = config.responseMocks.firstOrNull { it.matches(url, method) }
            if (responseMock != null) {
                request.attributes.put(MockedResponseKey, responseMock)
                if (responseMock.delayMs > 0) {
                    delay(responseMock.delayMs)
                }

                val bodyString = responseMock.bodyOverride ?: ""
                val bodyBytes = bodyString.toByteArray()

                val statusCode = HttpStatusCode.fromValue(responseMock.statusCode ?: 200)

                val responseData = HttpResponseData(
                    statusCode = statusCode,
                    requestTime = GMTDate(),
                    headers = Headers.build {
                        responseMock.headerOverrides.forEach { (key, value) -> append(key, value) }
                        if (!contains(HttpHeaders.ContentLength)) {
                            append(HttpHeaders.ContentLength, bodyBytes.size.toString())
                        }
                        if (!contains(HttpHeaders.ContentType)) {
                            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        }
                    },
                    version = HttpProtocolVersion.HTTP_1_1,
                    body = ByteReadChannel(bodyBytes),
                    callContext = request.executionContext
                )

                val transactionId = request.attributes.getOrNull(TransactionIdKey)
                if (transactionId != null) {
                    val startTime =
                        request.attributes.getOrNull(DebugStartTimeKey) ?: Clock.System.now()
                            .toEpochMilliseconds()
                    val endTime = Clock.System.now().toEpochMilliseconds()

                    val responseHeaders = mutableMapOf<String, String>()
                    responseData.headers.forEach { key, values ->
                        responseHeaders[key] = values.joinToString(", ")
                    }

                    val networkResponse = NetworkResponse(
                        statusCode = statusCode.value,
                        statusMessage = statusCode.description,
                        headers = responseHeaders,
                        body = bodyString,
                        bodySize = bodyBytes.size.toLong(),
                        responseTime = endTime
                    )

                    NetworkLogger.updateTransaction(transactionId) { existing ->
                        existing.copy(
                            response = networkResponse,
                            duration = endTime - startTime,
                            timestamp = endTime,
                            isMocked = true
                        )
                    }
                }

                val call = HttpClientCall(client, request.build(), responseData)
                return@intercept call
            }
        }

        execute(request)
    }

    onRequest { request, _ ->
        val config = DebugConfigManager.config.value

        val startTime = Clock.System.now().toEpochMilliseconds()
        request.attributes.put(DebugStartTimeKey, startTime)

        val url = request.url.toString()
        val method = request.method.value

        val transactionId = Uuid.random().toString()
        request.attributes.put(TransactionIdKey, transactionId)

        val requestHeaders = mutableMapOf<String, String>()
        request.headers.names().forEach { name ->
            requestHeaders[name] = request.headers.getAll(name)?.joinToString(", ") ?: ""
        }

        val queryParams = mutableMapOf<String, String>()
        request.url.parameters.names().forEach { name ->
            queryParams[name] = request.url.parameters.getAll(name)?.joinToString(", ") ?: ""
        }

        val networkRequest = NetworkRequest(
            url = url,
            method = method,
            headers = requestHeaders,
            queryParams = queryParams,
            body = null,
            bodySize = 0,
            requestTime = startTime
        )

        val initialTransaction = NetworkTransaction(
            id = transactionId,
            request = networkRequest,
            response = null,
            timestamp = startTime,
            duration = null,
            error = null,
            isMocked = false
        )

        NetworkLogger.logTransaction(initialTransaction)

        try {
            if (config.isOfflineMode) {
                throw Exception("Offline mode enabled - request cancelled")
            }

            if (config.throttleMs > 0) {
                delay(config.throttleMs)
            }

            if (!config.isEnabled) return@onRequest

            config.globalHeaders.forEach { (key, value) ->
                request.headers.append(key, value)
            }

            config.requestMocks
                .filter { it.matches(url, method) }
                .forEach { mock ->
                    mock.headerOverrides.forEach { (key, value) ->
                        request.headers.append(key, value)
                    }
                }
        } catch (e: Exception) {
            NetworkLogger.updateTransaction(transactionId) { existingTransaction ->
                existingTransaction.copy(
                    error = e.message,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            }
            throw e
        }
    }

    onResponse { response ->
        val isMockedViaAttribute = response.request.attributes.getOrNull(MockedResponseKey) != null
        if (isMockedViaAttribute) return@onResponse

        val startTime =
            response.request.attributes.getOrNull(DebugStartTimeKey) ?: Clock.System.now()
                .toEpochMilliseconds()
        val endTime = Clock.System.now().toEpochMilliseconds()
        val duration = endTime - startTime

        val transactionId =
            response.request.attributes.getOrNull(TransactionIdKey) ?: Uuid.random().toString()

        val responseHeaders = mutableMapOf<String, String>()
        response.headers.forEach { key, values ->
            responseHeaders[key] = values.joinToString(", ")
        }

        val responseBody = try {
            response.bodyAsText()
        } catch (e: Exception) {
            "[Binary or unreadable content]"
        }

        val networkResponse = NetworkResponse(
            statusCode = response.status.value,
            statusMessage = response.status.description,
            headers = responseHeaders,
            body = responseBody,
            bodySize = responseBody.length.toLong(),
            responseTime = Clock.System.now().toEpochMilliseconds()
        )

        NetworkLogger.updateTransaction(transactionId) { existingTransaction ->
            existingTransaction.copy(
                response = networkResponse,
                duration = duration,
                timestamp = endTime,
                isMocked = false
            )
        }
    }
}

fun createDebugHttpClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(DebugNetworkPlugin)
    }
}
