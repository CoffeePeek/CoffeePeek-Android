package com.coffeepeek.admin.locator

import com.coffeepeek.admin.config.AppConfig

object Constants {
    val BASE_URL: String get() = AppConfig.baseUrl
    const val DB_NAME = "fp.db"
}
