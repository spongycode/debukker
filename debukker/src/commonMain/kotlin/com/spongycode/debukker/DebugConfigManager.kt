package com.spongycode.debukker

import com.spongycode.debukker.models.Environment
import com.spongycode.debukker.models.EnvironmentConfig
import com.spongycode.debukker.models.RequestMock
import com.spongycode.debukker.models.ResponseMock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DebugConfig(
    val isEnabled: Boolean = true,
    val isResponseMockingEnabled: Boolean = true,
    val environmentConfig: EnvironmentConfig = EnvironmentConfig(),
    val requestMocks: List<RequestMock> = emptyList(),
    val responseMocks: List<ResponseMock> = emptyList(),
    val throttleMs: Long = 0,
    val isOfflineMode: Boolean = false,
    val urlFilters: List<String> = emptyList()
)

object DebugConfigManager {
    private val _config = MutableStateFlow(DebugConfig())
    val config: StateFlow<DebugConfig> = _config.asStateFlow()

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
                    currentEnvironment = environment.name
                )
            )
        }
    }

    fun setCustomBaseUrl(url: String) {
        _config.update {
            it.copy(
                environmentConfig = it.environmentConfig.copy(customBaseUrl = url)
            )
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
                }
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
                }
            )
        }
    }

    fun setThrottle(ms: Long) {
        _config.update { it.copy(throttleMs = ms) }
    }

    fun setOfflineMode(enabled: Boolean) {
        _config.update { it.copy(isOfflineMode = enabled) }
    }

    fun clearAllMocks() {
        _config.update {
            it.copy(
                requestMocks = emptyList(),
                responseMocks = emptyList()
            )
        }
    }
}
