package com.spongycode.debukker.debug.models

import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class NetworkTransaction(
    val id: String,
    val request: NetworkRequest,
    val response: NetworkResponse? = null,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val duration: Long? = null,
    val error: String? = null,
    val isMocked: Boolean = false
)

@Serializable
data class NetworkRequest(
    val url: String,
    val method: String,
    val headers: Map<String, String>,
    val queryParams: Map<String, String>,
    val body: String? = null,
    val bodySize: Long = 0,
    val requestTime: Long = 0
)

@Serializable
data class NetworkResponse(
    val statusCode: Int,
    val statusMessage: String = "",
    val headers: Map<String, String>,
    val body: String? = null,
    val bodySize: Long = 0,
    val responseTime: Long = 0
)
