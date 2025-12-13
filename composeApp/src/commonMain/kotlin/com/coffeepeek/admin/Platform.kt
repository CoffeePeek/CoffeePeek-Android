package com.coffeepeek.admin

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform