package com.spongycode.debukker.debug.preferences

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DebugPreferencesManager {
    private val dataSource = PreferencesDataSource()

    private val _preferences = MutableStateFlow<Map<String, String>>(emptyMap())
    val preferences: StateFlow<Map<String, String>> = _preferences.asStateFlow()

    fun refresh() {
        _preferences.value = dataSource.getAllPreferences()
    }

    fun updatePreference(key: String, value: String) {
        dataSource.setPreference(key, value)
        refresh()
    }

    fun deletePreference(key: String) {
        dataSource.deletePreference(key)
        refresh()
    }

    fun clearAll() {
        dataSource.clearAll()
        refresh()
    }
}
