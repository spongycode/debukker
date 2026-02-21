package com.spongycode.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

import com.spongycode.debukker.network.createDebugHttpClient
import com.spongycode.debukker.ui.DraggableDebugButton

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    DebugFacade.DebukkerUI = { DraggableDebugButton() }
    DebugFacade.httpClientFactory = { createDebugHttpClient() }
    ComposeViewport {
        App()
    }
}