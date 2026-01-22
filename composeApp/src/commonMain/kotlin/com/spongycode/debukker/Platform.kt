package com.spongycode.debukker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform