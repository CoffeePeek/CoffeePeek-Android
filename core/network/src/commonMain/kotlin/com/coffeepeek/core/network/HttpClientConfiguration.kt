package com.coffeepeek.core.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val networkJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

/** Shared transport configuration; engines and authentication are supplied by consumers. */
fun HttpClientConfig<*>.configureApiTransport(baseUrl: String) {
    install(ContentNegotiation) {
        json(networkJson)
    }
    defaultRequest { url(baseUrl.trim().trimEnd('/')) }
}

/** Presigned uploads use their own client without API authentication or a base URL. */
fun HttpClientConfig<*>.configureUploadTransport() {
    install(HttpTimeout) {
        requestTimeoutMillis = 120_000
        connectTimeoutMillis = 30_000
        socketTimeoutMillis = 120_000
    }
}
