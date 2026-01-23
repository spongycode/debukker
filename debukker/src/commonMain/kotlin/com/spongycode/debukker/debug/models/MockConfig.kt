package com.spongycode.debukker.debug.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestMock(
    val id: String,
    val urlPattern: String,
    val method: String? = null,
    val headerOverrides: Map<String, String> = emptyMap(),
    val bodyOverride: String? = null,
    val isEnabled: Boolean = true
) {
    fun matches(url: String, method: String): Boolean {
        if (!isEnabled) return false

        val urlMatches = try {
            Regex(urlPattern).containsMatchIn(url)
        } catch (e: Exception) {
            url.contains(urlPattern, ignoreCase = true)
        }

        val methodMatches = this.method == null || this.method.equals(method, ignoreCase = true)

        return urlMatches && methodMatches
    }
}

@Serializable
data class ResponseMock(
    val id: String,
    val urlPattern: String,
    val method: String? = null,
    val statusCode: Int? = null,
    val headerOverrides: Map<String, String> = emptyMap(),
    val bodyOverride: String? = null,
    val delayMs: Long = 0,
    val isEnabled: Boolean = true
) {
    fun matches(url: String, method: String): Boolean {
        if (!isEnabled) return false

        val urlMatches = try {
            Regex(urlPattern).containsMatchIn(url)
        } catch (e: Exception) {
            url.contains(urlPattern, ignoreCase = true)
        }

        val methodMatches = this.method == null || this.method.equals(method, ignoreCase = true)

        return urlMatches && methodMatches
    }
}
