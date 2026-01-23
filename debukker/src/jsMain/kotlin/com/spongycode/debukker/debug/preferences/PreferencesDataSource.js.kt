package com.spongycode.debukker.debug.preferences

import kotlinx.browser.localStorage

actual class PreferencesDataSource {
    actual fun getAllPreferences(): Map<String, String> {
        val prefs = mutableMapOf<String, String>()
        for (i in 0 until localStorage.length) {
            val key = localStorage.key(i)
            if (key != null) {
                localStorage.getItem(key)?.let { value ->
                    prefs[key] = value
                }
            }
        }
        return prefs
    }

    actual fun setPreference(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    actual fun deletePreference(key: String) {
        localStorage.removeItem(key)
    }

    actual fun clearAll() {
        localStorage.clear()
    }
}
