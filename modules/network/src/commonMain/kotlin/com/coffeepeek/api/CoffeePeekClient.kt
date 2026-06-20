package com.coffeepeek.api

import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.api.service.AuthService
import com.coffeepeek.api.utils.CurlInterceptor.asCurlString
import com.coffeepeek.api.utils.JsonExt
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import java.io.File

internal expect fun createClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient

class CoffeePeekClient(
    url: String,
    cacheFolder: File,
    private val debug: Boolean,
    private val getToken: () -> AuthResp?,
    private val saveToken: (AuthResp?) -> Unit,
) {
    val plainClient: HttpClient = createClient {
        install(ContentNegotiation) { json(json = JsonExt.json) }
        defaultRequest { url(url) }
    }.also { intercept(it) }

    val authService: AuthService by lazy { AuthService(client, plainClient) }

    val client: HttpClient = createClient {
        install(ContentNegotiation) { json(json = JsonExt.json) }
        defaultRequest { url(url) }

        install(Auth) {
            bearer {
                loadTokens {
                    getToken()?.let { BearerTokens(it.accessToken, it.refreshToken) }
                }
                refreshTokens {
                    val oldTokens = getToken() ?: return@refreshTokens null
                    try {
                        val newTokens = authService.refresh(oldTokens.refreshToken).getOrThrow()
                        saveToken(newTokens)
                        BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        }

        install(HttpCache) {
            publicStorage(FileStorage(cacheFolder))
        }
    }.also { intercept(it) }

    private fun intercept(httpClient: HttpClient) {
        if (debug) {
            httpClient.plugin(HttpSend).intercept {
                val message = it.asCurlString()
                println(message)
                execute(it).also { responseCall ->
                    println("CURL ${responseCall.response.status.value}")
                }
            }
        }
    }
}
