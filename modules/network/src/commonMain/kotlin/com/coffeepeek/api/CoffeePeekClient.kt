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
    private val saveToken: (AuthResp?) -> Unit
) {
    private val refreshClient = createClient {
        install(ContentNegotiation) { json(json = JsonExt.json) }
        defaultRequest {
            url(url)
        }
    }.also { intercept(it) }

    private val authService = AuthService(refreshClient)

    val client = createClient {
        install(ContentNegotiation) { json(json = JsonExt.json) }

        defaultRequest {
            url(url)
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val tokens = getToken()
                    if (tokens != null) {
                        BearerTokens(tokens.accessToken, tokens.refreshToken)
                    } else null
                }
                refreshTokens {
                    val oldTokens = getToken() ?: return@refreshTokens null
                    try {
                        val newTokens = authService.authRefresh(oldTokens.refreshToken).getOrThrow()
                        saveToken(newTokens)

                        BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                    } catch (e: Exception) {
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
        if (debug) httpClient.plugin(HttpSend).intercept {
            val message = it.asCurlString()
            println(message)
            execute(it).also { responseCall ->
                val resp = responseCall.response.bodyAsText(Charsets.UTF_8)
                println("CURL ${responseCall.response.status.value}")
            }
        }
    }

}