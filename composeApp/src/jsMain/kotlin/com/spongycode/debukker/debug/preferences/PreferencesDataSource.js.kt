package com.spongycode.debukker.debug.preferences

import kotlinx.browser.localStorage

actual class PreferencesDataSource {
    private val prefix = ""
    
    actual fun getAllPreferences(): Map<String, String> {
        val prefs = mutableMapOf<String, String>()
        for (i in 0 until localStorage.length) {
            val key = localStorage.key(i)
            if (key != null && key.startsWith(prefix)) {
                val actualKey = key.removePrefix(prefix)
                val value = localStorage.getItem(key)
                if (value != null) {
                    prefs[actualKey] = value
                }
            }
        }
        return prefs
    }
    
    actual fun setPreference(key: String, value: String) {
        localStorage.setItem(prefix + key, value)
    }
    
    actual fun deletePreference(key: String) {
        localStorage.removeItem(prefix + key)
    }
    
    actual fun clearAll() {
        val keysToRemove = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            val key = localStorage.key(i)
            if (key != null && key.startsWith(prefix)) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { localStorage.removeItem(it) }
    }
}
