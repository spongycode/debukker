package com.spongycode.debukker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.ui.graphics.vector.ImageVector
import com.spongycode.debukker.models.Environment
import com.spongycode.debukker.models.EnvironmentConfig
import com.spongycode.debukker.models.RequestMock
import com.spongycode.debukker.models.ResponseMock
import com.spongycode.debukker.preferences.PreferencesDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class DebugModule(val displayName: String, val icon: ImageVector) {
    NETWORK_LOGS("Network", Icons.Default.SwapVert),
    MOCK_CONFIG("Mocking", Icons.Default.Dataset),
    ENVIRONMENT("Environment", Icons.Default.Dns),
    PREFERENCES("Keys", Icons.Default.Key)
}

data class DebugConfig(
    val isEnabled: Boolean = true,
    val isResponseMockingEnabled: Boolean = true,
    val environmentConfig: EnvironmentConfig = EnvironmentConfig(),
    val requestMocks: List<RequestMock> = emptyList(),
    val responseMocks: List<ResponseMock> = emptyList(),
    val throttleMs: Long = 0,
    val isOfflineMode: Boolean = false,
    val urlFilters: List<String> = emptyList(),
    val globalHeaders: Map<String, String> = emptyMap(),
    val modules: List<DebugModule> = DebugModule.entries,
    val requestTimeoutMs: Long = 0,
    val connectTimeoutMs: Long = 0,
    val socketTimeoutMs: Long = 0
)

object DebugConfigManager {
    private val _config = MutableStateFlow(DebugConfig())
    val config: StateFlow<DebugConfig> = _config.asStateFlow()

    var onEnvironmentChange: ((Environment) -> Unit)? = null
    var onPreferencesRefresh: (() -> Unit)? = null
    
    // We instantiate the PreferencesDataSource internally since debukker uses actual/expect for preferences
    private val preferencesDataSource = PreferencesDataSource()

    private const val KEY_CURRENT_ENV = "debug_current_env"
    private const val KEY_CUSTOM_URL = "debug_custom_url"

    fun loadPersistedConfig() {
        val savedEnv = preferencesDataSource.getAllPreferences()[KEY_CURRENT_ENV]
        val savedUrl = preferencesDataSource.getAllPreferences()[KEY_CUSTOM_URL]

        _config.update { current ->
            var updatedConfig = current
            if (savedUrl != null) {
                updatedConfig = updatedConfig.copy(
                    environmentConfig = updatedConfig.environmentConfig.copy(customBaseUrl = savedUrl)
                )
            }
            if (savedEnv != null) {
                updatedConfig = updatedConfig.copy(
                    environmentConfig = updatedConfig.environmentConfig.copy(currentEnvironment = savedEnv)
                )
            }
            updatedConfig
        }

        val finalConfig = _config.value.environmentConfig
        val envToApply = if (finalConfig.currentEnvironment == Environment.CUSTOM.name) {
            Environment.CUSTOM
        } else {
            Environment.entries.find { it.name == finalConfig.currentEnvironment } ?: Environment.PRODUCTION
        }
        onEnvironmentChange?.invoke(envToApply)
    }

    fun setEnabled(enabled: Boolean) {
        _config.update { it.copy(isEnabled = enabled) }
    }

    fun setResponseMockingEnabled(enabled: Boolean) {
        _config.update { it.copy(isResponseMockingEnabled = enabled) }
    }

    fun updateEnvironment(environment: Environment) {
        _config.update {
            it.copy(
                environmentConfig = it.environmentConfig.copy(
                    currentEnvironment = environment.name,
                ),
            )
        }
        preferencesDataSource.setPreference(KEY_CURRENT_ENV, environment.name)
        onEnvironmentChange?.invoke(environment)
    }

    fun setCustomBaseUrl(url: String) {
        _config.update {
            it.copy(
                environmentConfig = it.environmentConfig.copy(customBaseUrl = url),
            )
        }
        preferencesDataSource.setPreference(KEY_CUSTOM_URL, url)
        if (_config.value.environmentConfig.currentEnvironment == Environment.CUSTOM.name) {
            onEnvironmentChange?.invoke(Environment.CUSTOM)
        }
    }

    fun addRequestMock(mock: RequestMock) {
        _config.update { it.copy(requestMocks = it.requestMocks + mock) }
    }

    fun removeRequestMock(id: String) {
        _config.update {
            it.copy(requestMocks = it.requestMocks.filter { mock -> mock.id != id })
        }
    }

    fun updateRequestMock(mock: RequestMock) {
        _config.update {
            it.copy(
                requestMocks = it.requestMocks.map { existing ->
                    if (existing.id == mock.id) mock else existing
                },
            )
        }
    }

    fun addResponseMock(mock: ResponseMock) {
        _config.update { current ->
            val filteredMocks = current.responseMocks.filterNot {
                it.urlPattern == mock.urlPattern && it.method == mock.method
            }
            current.copy(responseMocks = filteredMocks + mock)
        }
    }

    fun removeResponseMock(id: String) {
        _config.update {
            it.copy(responseMocks = it.responseMocks.filter { mock -> mock.id != id })
        }
    }

    fun updateResponseMock(mock: ResponseMock) {
        _config.update {
            it.copy(
                responseMocks = it.responseMocks.map { existing ->
                    if (existing.id == mock.id) mock else existing
                },
            )
        }
    }

    fun updateGlobalHeaders(headers: Map<String, String>) {
        _config.update { it.copy(globalHeaders = headers) }
    }

    fun setThrottle(ms: Long) {
        _config.update { it.copy(throttleMs = ms) }
    }

    fun setOfflineMode(enabled: Boolean) {
        _config.update { it.copy(isOfflineMode = enabled) }
    }

    fun setModules(modules: List<DebugModule>) {
        _config.update { it.copy(modules = modules) }
    }

    fun setTimeoutOverrides(request: Long, connect: Long, socket: Long) {
        _config.update {
            it.copy(
                requestTimeoutMs = request,
                connectTimeoutMs = connect,
                socketTimeoutMs = socket
            )
        }
    }

    fun clearAllMocks() {
        _config.update {
            it.copy(
                requestMocks = emptyList(),
                responseMocks = emptyList(),
                globalHeaders = emptyMap(),
            )
        }
    }
}
