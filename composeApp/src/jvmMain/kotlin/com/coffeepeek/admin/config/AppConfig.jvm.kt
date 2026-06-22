package com.coffeepeek.admin.config

actual object AppConfig {
    actual val versionName: String = JvmAppConfig.VERSION_NAME
    actual val baseUrl: String = JvmAppConfig.BASE_URL
}
