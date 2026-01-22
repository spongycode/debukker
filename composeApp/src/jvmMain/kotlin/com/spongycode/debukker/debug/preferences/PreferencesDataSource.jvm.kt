package com.spongycode.debukker.debug.preferences

import java.util.prefs.Preferences

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PreferencesDataSource {
    private val prefs = Preferences.userRoot().node("com/spongycode/debugsampleapp/debug")
    
    actual fun getAllPreferences(): Map<String, String> {
        return try {
            prefs.keys().associateWith { key ->
                prefs.get(key, "")
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    actual fun setPreference(key: String, value: String) {
        prefs.put(key, value)
        prefs.flush()
    }
    
    actual fun deletePreference(key: String) {
        prefs.remove(key)
        prefs.flush()
    }
    
    actual fun clearAll() {
        try {
            prefs.clear()
            prefs.flush()
        } catch (e: Exception) {
            // Handle exception
        }
    }
}
