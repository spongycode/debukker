package com.spongycode.debukker.preferences

import platform.Foundation.NSUserDefaults

actual class PreferencesDataSource {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    actual fun getAllPreferences(): Map<String, String> {
        val dict = userDefaults.dictionaryRepresentation()
        return dict.mapNotNull { (key, value) ->
            if (key is String) {
                key to value.toString()
            } else null
        }.toMap()
    }
    
    actual fun setPreference(key: String, value: String) {
        userDefaults.setObject(value, forKey = key)
        userDefaults.synchronize()
    }
    
    actual fun deletePreference(key: String) {
        userDefaults.removeObjectForKey(key)
        userDefaults.synchronize()
    }
    
    actual fun clearAll() {
        val dict = userDefaults.dictionaryRepresentation()
        dict.keys.forEach { key ->
            if (key is String) {
                userDefaults.removeObjectForKey(key)
            }
        }
        userDefaults.synchronize()
    }
}
