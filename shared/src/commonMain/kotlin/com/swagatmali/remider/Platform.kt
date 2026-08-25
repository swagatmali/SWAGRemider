package com.swagatmali.remider

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform