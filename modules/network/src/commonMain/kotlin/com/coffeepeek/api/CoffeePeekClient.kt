package com.coffeepeek.api

import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.api.service.AuthService
import com.coffeepeek.api.utils.CurlInterceptor.asCurlString
import com.coffeepeek.api.utils.httpDebugLog
import com.coffeepeek.core.network.configureApiTransport
import com.coffeepeek.core.network.configureUploadTransport
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.plugin

internal expect fun createClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient

internal expect fun createUploadClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient

internal expect fun createHttpCacheStorage(cacheFolderPath: String): CacheStorage?

class CoffeePeekClient(
    url: String,
    cacheFolderPath: String,
    private val debug: Boolean,
    private val getToken: () -> AuthResp?,
    private val saveToken: (AuthResp?) -> Unit,
) {
    private val baseUrl = normalizeBaseUrl(url)

    private fun resolveTokens(): AuthResp? = getToken()

    private fun persistTokens(tokens: AuthResp?) {
        saveToken(tokens)
    }

    val plainClient: HttpClient = createClient {
        configureApiTransport(baseUrl)
    }.also { intercept(it) }

    val uploadClient: HttpClient = createUploadClient {
        configureUploadTransport()
    }.also { intercept(it) }

    private val tokenRefreshService = AuthService(plainClient, plainClient)

    val client: HttpClient = createClient {
        configureApiTransport(baseUrl)

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
            createHttpCacheStorage(cacheFolderPath)?.let(::publicStorage)
        }
    }.also { intercept(it) }

    val authService: AuthService by lazy { AuthService(client, plainClient) }

    private fun intercept(httpClient: HttpClient) {
        if (debug) {
            httpClient.plugin(HttpSend).intercept { request ->
                val message = request.asCurlString()
                httpDebugLog(message)
                execute(request).also { responseCall ->
                    httpDebugLog("CURL ${responseCall.response.status.value}")
                }
            }
        }
    }
}

internal fun normalizeBaseUrl(url: String): String = url.trim().trimEnd('/')
