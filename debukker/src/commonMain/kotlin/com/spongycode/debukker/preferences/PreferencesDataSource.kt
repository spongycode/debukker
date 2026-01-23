package com.spongycode.debukker.preferences

expect class PreferencesDataSource() {
    fun getAllPreferences(): Map<String, String>
    fun setPreference(key: String, value: String)
    fun deletePreference(key: String)
    fun clearAll()
}
