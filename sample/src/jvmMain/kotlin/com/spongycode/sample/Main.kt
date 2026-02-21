package com.spongycode.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    initDebukker()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Debukker Sample"
    ) {
        App()
    }
}
