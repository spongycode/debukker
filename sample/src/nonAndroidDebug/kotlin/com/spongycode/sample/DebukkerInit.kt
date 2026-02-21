package com.spongycode.sample

import com.spongycode.debukker.network.createDebugHttpClient
import com.spongycode.debukker.ui.DraggableDebugButton

fun initDebukker() {
    DebugFacade.DebukkerUI = { DraggableDebugButton() }
    DebugFacade.httpClientFactory = { createDebugHttpClient() }
}
