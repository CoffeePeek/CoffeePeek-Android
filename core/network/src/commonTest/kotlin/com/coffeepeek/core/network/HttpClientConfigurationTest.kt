package com.coffeepeek.core.network

import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HttpClientConfigurationTest {
    @Test
    fun apiTransportResolvesRelativePathAndDecodesJson() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("https://example.com/api/items", request.url.toString())
            respond("{\"count\":3}", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClientFactory(engine).api("  https://example.com/  ")
        try {
            assertEquals(mapOf("count" to 3), client.get("/api/items").body<Map<String, Int>>())
        } finally {
            client.close()
            engine.close()
        }
    }

    @Test
    fun uploadTransportKeepsAbsoluteStorageUrlAndTimeouts(): Unit = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("https://storage.example.com/upload?signature=value", request.url.toString())
            assertEquals(null, request.headers[HttpHeaders.Authorization])
            val timeouts = assertNotNull(request.getCapabilityOrNull(HttpTimeoutCapability))
            assertEquals(120_000L, timeouts.requestTimeoutMillis)
            assertEquals(30_000L, timeouts.connectTimeoutMillis)
            assertEquals(120_000L, timeouts.socketTimeoutMillis)
            respond("")
        }
        val client = HttpClientFactory(engine).upload()
        try {
            client.get("https://storage.example.com/upload?signature=value")
        } finally {
            client.close()
            engine.close()
        }
    }
}
