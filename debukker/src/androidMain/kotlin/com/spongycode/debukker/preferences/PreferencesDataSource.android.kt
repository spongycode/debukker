package com.spongycode.debukker.preferences

import android.content.Context
import android.content.SharedPreferences

private var sharedPreferences: SharedPreferences? = null


fun initializePreferences(context: Context) {
    sharedPreferences = context.getSharedPreferences("debug_prefs", Context.MODE_PRIVATE)
}

actual class PreferencesDataSource {
    actual fun getAllPreferences(): Map<String, String> {
        val prefs = sharedPreferences ?: return emptyMap()
        return prefs.all.mapValues { (_, value) -> value.toString() }
    }
    
    actual fun setPreference(key: String, value: String) {
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }
    
    actual fun deletePreference(key: String) {
        sharedPreferences?.edit()?.remove(key)?.apply()
    }
    
    actual fun clearAll() {
        sharedPreferences?.edit()?.clear()?.apply()
    }
}
