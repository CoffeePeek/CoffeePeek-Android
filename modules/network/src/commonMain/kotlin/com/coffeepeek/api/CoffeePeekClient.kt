package com.coffeepeek.api

import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.api.service.AuthService
import com.coffeepeek.api.utils.CurlInterceptor.asCurlString
import com.coffeepeek.api.utils.JsonExt
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.serialization.kotlinx.json.json
import java.io.File

internal expect fun createClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient

internal expect fun createUploadClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient

class CoffeePeekClient(
    url: String,
    cacheFolder: File,
    private val debug: Boolean,
    private val getToken: () -> AuthResp?,
    private val saveToken: (AuthResp?) -> Unit,
) {
    @Volatile
    private var cachedTokens: AuthResp? = null

    private fun resolveTokens(): AuthResp? =
        cachedTokens ?: getToken()?.also { cachedTokens = it }

    private fun persistTokens(tokens: AuthResp?) {
        cachedTokens = tokens
        saveToken(tokens)
    }

    val plainClient: HttpClient = createClient {
        install(ContentNegotiation) { json(json = JsonExt.json) }
        defaultRequest { url(url) }
    }.also { intercept(it) }

    val uploadClient: HttpClient = createUploadClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 120_000
        }
    }.also { intercept(it) }

    private val tokenRefreshService = AuthService(plainClient, plainClient)

    val client: HttpClient = createClient {
        install(ContentNegotiation) { json(json = JsonExt.json) }
        defaultRequest { url(url) }

        install(Auth) {
            bearer {
                loadTokens {
                    resolveTokens()?.let { BearerTokens(it.accessToken, it.refreshToken) }
                }
                refreshTokens {
                    val oldTokens = resolveTokens() ?: return@refreshTokens null
                    if (oldTokens.refreshToken.isBlank()) {
                        persistTokens(null)
                        return@refreshTokens null
                    }
                    try {
                        val newTokens = tokenRefreshService.refresh(oldTokens.refreshToken).getOrThrow()
                        persistTokens(newTokens)
                        BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                    } catch (_: Exception) {
                        persistTokens(null)
                        null
                    }
                }
            }
        }

        install(HttpCache) {
            publicStorage(FileStorage(cacheFolder))
        }
    }.also { intercept(it) }

    val authService: AuthService by lazy { AuthService(client, plainClient) }

    private fun intercept(httpClient: HttpClient) {
        if (debug) {
            httpClient.plugin(HttpSend).intercept { request ->
                val message = request.asCurlString()
                println(message)
                execute(request).also { responseCall ->
                    println("CURL ${responseCall.response.status.value}")
                }
            }
        }
    }
}
