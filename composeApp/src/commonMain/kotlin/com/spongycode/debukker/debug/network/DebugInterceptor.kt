package com.spongycode.debukker.debug.network

import com.spongycode.debukker.debug.DebugConfigManager
import com.spongycode.debukker.debug.models.NetworkRequest
import com.spongycode.debukker.debug.models.NetworkResponse
import com.spongycode.debukker.debug.models.NetworkTransaction
import com.spongycode.debukker.debug.models.ResponseMock
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val DebugStartTimeKey = AttributeKey<Long>("DebugStartTime")
private val MockedResponseKey = AttributeKey<ResponseMock>("MockedResponse")

@OptIn(ExperimentalUuidApi::class)
val DebugNetworkPlugin = createClientPlugin("DebugNetworkPlugin") {

    onRequest { request, _ ->
        val config = DebugConfigManager.config.value

        val startTime = Clock.System.now().toEpochMilliseconds()
        request.attributes.put(DebugStartTimeKey, startTime)

        val url = request.url.toString()
        val method = request.method.value

        val transactionId = Uuid.random().toString()
        request.attributes.put(AttributeKey("TransactionId"), transactionId)

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

            if (!config.isEnabled || !config.isResponseMockingEnabled) return@onRequest

            config.requestMocks
                .filter { it.matches(url, method) }
                .forEach { mock ->
                    mock.headerOverrides.forEach { (key, value) ->
                        request.headers.append(key, value)
                    }
                }

            val responseMock = config.responseMocks.firstOrNull { it.matches(url, method) }
            if (responseMock != null) {
                request.attributes.put(MockedResponseKey, responseMock)

                if (responseMock.delayMs > 0) {
                    delay(responseMock.delayMs)
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
        val config = DebugConfigManager.config.value

        val startTime = response.request.attributes[DebugStartTimeKey]
        val endTime = Clock.System.now().toEpochMilliseconds()
        val duration = endTime - startTime

        val transactionId =
            response.request.attributes.getOrNull(AttributeKey<String>("TransactionId"))
                ?: Uuid.random().toString()

        val httpRequest = response.request
        val url = httpRequest.url.toString()
        val method = httpRequest.method.value

        val requestHeaders = mutableMapOf<String, String>()
        httpRequest.headers.forEach { key, values ->
            requestHeaders[key] = values.joinToString(", ")
        }

        val queryParams = mutableMapOf<String, String>()
        httpRequest.url.parameters.forEach { key, values ->
            queryParams[key] = values.joinToString(", ")
        }

        val responseMock = if (config.isEnabled && config.isResponseMockingEnabled) {
            response.request.attributes.getOrNull(MockedResponseKey)
        } else null
        val isMocked = responseMock != null

        val responseHeaders = mutableMapOf<String, String>()
        response.headers.forEach { key, values ->
            responseHeaders[key] = values.joinToString(", ")
        }

        responseMock?.headerOverrides?.forEach { (key, value) ->
            responseHeaders[key] = value
        }

        val responseBody = try {
            val originalBody = response.bodyAsText()
            responseMock?.bodyOverride ?: originalBody
        } catch (e: Exception) {
            "[Binary or unreadable content]"
        }

        val statusCode = responseMock?.statusCode ?: response.status.value
        val statusMessage = if (responseMock?.statusCode != null) {
            HttpStatusCode.fromValue(statusCode).description
        } else {
            response.status.description
        }

        val networkResponse = NetworkResponse(
            statusCode = statusCode,
            statusMessage = statusMessage,
            headers = responseHeaders,
            body = responseBody,
            bodySize = responseBody.length.toLong(),
            responseTime = response.responseTime.timestamp
        )

        NetworkLogger.updateTransaction(transactionId) { existingTransaction ->
            existingTransaction.copy(
                response = networkResponse,
                duration = duration,
                timestamp = endTime,
                isMocked = isMocked
            )
        }
    }
}

fun createDebugHttpClient(): HttpClient {
    return HttpClient {
        install(DebugNetworkPlugin)
    }
}
