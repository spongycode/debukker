package com.spongycode.debukker.debug.network

import com.spongycode.debukker.debug.DebugConfigManager
import com.spongycode.debukker.debug.models.NetworkRequest
import com.spongycode.debukker.debug.models.NetworkResponse
import com.spongycode.debukker.debug.models.NetworkTransaction
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.util.AttributeKey
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val DebugStartTimeKey = AttributeKey<Long>("DebugStartTime")

@OptIn(ExperimentalUuidApi::class)
val DebugNetworkPlugin = createClientPlugin("DebugNetworkPlugin") {

    onResponse { response ->
        val config = DebugConfigManager.config.value
        if (!config.isEnabled) return@onResponse

        val startTime = response.request.attributes[DebugStartTimeKey]
        val endTime = Clock.System.now().toEpochMilliseconds()
        val duration = endTime - startTime

        val httpRequest = response.request
        val url = httpRequest.url.toString()
        val method = httpRequest.method.value

        val transactionId = Uuid.random().toString()

        val requestHeaders = mutableMapOf<String, String>()
        httpRequest.headers.forEach { key, values ->
            requestHeaders[key] = values.joinToString(", ")
        }

        val queryParams = mutableMapOf<String, String>()
        httpRequest.url.parameters.forEach { key, values ->
            queryParams[key] = values.joinToString(", ")
        }

        val responseHeaders = mutableMapOf<String, String>()
        response.headers.forEach { key, values ->
            responseHeaders[key] = values.joinToString(", ")
        }
        val responseBody = try {
            response.bodyAsText()
        } catch (e: Exception) {
            "[Binary or unreadable content]"
        }

        val networkRequest = NetworkRequest(
            url = url,
            method = method,
            headers = requestHeaders,
            queryParams = queryParams,
            body = null,
            bodySize = 0,
            requestTime = response.requestTime.timestamp
        )

        val networkResponse = NetworkResponse(
            statusCode = response.status.value,
            statusMessage = response.status.description,
            headers = responseHeaders,
            body = responseBody,
            bodySize = responseBody.length.toLong(),
            responseTime = response.responseTime.timestamp
        )

        val isMocked = config.responseMocks.any { it.matches(url, method) }

        val transaction = NetworkTransaction(
            id = transactionId,
            request = networkRequest,
            response = networkResponse,
            timestamp = endTime,
            duration = duration,
            error = null,
            isMocked = isMocked
        )

        NetworkLogger.logTransaction(transaction)
    }

    onRequest { request, _ ->
        val config = DebugConfigManager.config.value
        if (!config.isEnabled) return@onRequest

        request.attributes.put(
            DebugStartTimeKey,
            Clock.System.now().toEpochMilliseconds()
        )
        val url = request.url.toString()
        val method = request.method.value

        if (config.isOfflineMode) {
            throw Exception("Offline mode enabled - request cancelled")
        }

        if (config.throttleMs > 0) {
            delay(config.throttleMs)
        }

        config.requestMocks
            .filter { it.matches(url, method) }
            .forEach { mock ->
                mock.headerOverrides.forEach { (key, value) ->
                    request.headers.append(key, value)
                }
            }
    }
}

fun createDebugHttpClient(): HttpClient {
    return HttpClient {
        install(DebugNetworkPlugin)
    }
}
