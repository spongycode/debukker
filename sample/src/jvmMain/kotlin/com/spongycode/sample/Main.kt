package com.spongycode.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import com.spongycode.debukker.network.createDebugHttpClient
import com.spongycode.debukker.ui.DraggableDebugButton

fun main() = application {
    DebugFacade.DebukkerUI = { DraggableDebugButton() }
    DebugFacade.httpClientFactory = { createDebugHttpClient() }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Debukker Sample"
    ) {
        App()
    }
}
