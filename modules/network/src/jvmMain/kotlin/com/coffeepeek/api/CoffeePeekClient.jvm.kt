package com.coffeepeek.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO



internal actual fun createClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(CIO){
        block()
    }
}