package com.coffeepeek.admin.config

import com.coffeepeek.BuildConfig

actual object AppConfig {
    actual val versionName: String = BuildConfig.VERSION_NAME
    actual val baseUrl: String = BuildConfig.API_BASE_URL
}
