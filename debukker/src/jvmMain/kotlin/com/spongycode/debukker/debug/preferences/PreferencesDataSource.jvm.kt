package com.spongycode.debukker.debug.preferences

actual class PreferencesDataSource {
    private val preferences = mutableMapOf<String, String>()

    actual fun getAllPreferences(): Map<String, String> {
        return preferences.toMap()
    }

    actual fun setPreference(key: String, value: String) {
        preferences[key] = value
    }

    actual fun deletePreference(key: String) {
        preferences.remove(key)
    }

    actual fun clearAll() {
        preferences.clear()
    }
}
