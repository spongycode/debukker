package com.spongycode.sample

import androidx.compose.ui.window.ComposeUIViewController

import com.spongycode.debukker.network.createDebugHttpClient
import com.spongycode.debukker.ui.DraggableDebugButton

fun MainViewController() = ComposeUIViewController {
    DebugFacade.DebukkerUI = { DraggableDebugButton() }
    DebugFacade.httpClientFactory = { createDebugHttpClient() }
    App() 
}
