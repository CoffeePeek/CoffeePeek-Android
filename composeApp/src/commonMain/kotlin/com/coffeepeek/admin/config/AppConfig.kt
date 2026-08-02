package com.coffeepeek.admin.config

expect object AppConfig {
    val versionName: String
    val baseUrl: String
    val isDebug: Boolean
}
