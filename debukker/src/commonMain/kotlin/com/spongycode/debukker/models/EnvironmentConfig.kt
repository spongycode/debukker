package com.spongycode.debukker.models

import kotlinx.serialization.Serializable

enum class Environment(val displayName: String) {
    PRODUCTION("Production"),
    PRE_PRODUCTION("Pre-production"),
    LOCAL("Local"),
    CUSTOM("Custom")
}

@Serializable
data class EnvironmentConfig(
    val currentEnvironment: String = Environment.PRODUCTION.name,
    val customBaseUrl: String = "",
    val productionUrl: String = "https://api.example.com",
    val preProductionUrl: String = "https://preprod-api.example.com",
    val localUrl: String = "http://localhost:8080"
) {
    fun getBaseUrl(): String {
        return when (currentEnvironment) {
            Environment.PRODUCTION.name -> productionUrl
            Environment.PRE_PRODUCTION.name -> preProductionUrl
            Environment.LOCAL.name -> localUrl
            Environment.CUSTOM.name -> customBaseUrl
            else -> productionUrl
        }
    }
    
    fun getCurrentEnvironment(): Environment {
        return Environment.entries.find { it.name == currentEnvironment }
            ?: Environment.PRODUCTION
    }
}
