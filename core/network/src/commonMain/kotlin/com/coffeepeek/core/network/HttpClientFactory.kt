package com.coffeepeek.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine

/** The caller owns the returned client and closes it at the end of its lifecycle. */
class HttpClientFactory(private val engine: HttpClientEngine) {
    fun api(
        baseUrl: String,
        configure: HttpClientConfig<*>.() -> Unit = {},
    ): HttpClient = HttpClient(engine) {
        configureApiTransport(baseUrl)
        configure()
    }

    fun upload(configure: HttpClientConfig<*>.() -> Unit = {}): HttpClient = HttpClient(engine) {
        configureUploadTransport()
        configure()
    }
}
