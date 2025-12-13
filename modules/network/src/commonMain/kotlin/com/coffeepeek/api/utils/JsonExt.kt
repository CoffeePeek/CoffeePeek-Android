package com.coffeepeek.api.utils

import kotlinx.serialization.json.Json

internal object JsonExt {

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

}