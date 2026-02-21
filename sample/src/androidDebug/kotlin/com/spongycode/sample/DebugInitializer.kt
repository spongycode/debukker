package com.spongycode.sample

import android.content.Context
import com.spongycode.debukker.network.createDebugHttpClient
import com.spongycode.debukker.preferences.initializePreferences
import com.spongycode.debukker.ui.DraggableDebugButton

object DebugInitializer {
    fun init(context: Context) {
        initializePreferences(context)
        DebugFacade.DebukkerUI = { DraggableDebugButton() }
        DebugFacade.httpClientFactory = { createDebugHttpClient() }
    }
}
