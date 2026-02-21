package com.spongycode.sample

import androidx.compose.runtime.Composable
import io.ktor.client.HttpClient

object DebugFacade {
    var DebukkerUI: @Composable () -> Unit = {}
    var httpClientFactory: () -> HttpClient = { HttpClient() }
}
