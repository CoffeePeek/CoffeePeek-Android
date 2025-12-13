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
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.AuthScheme
import io.ktor.serialization.kotlinx.json.json
import java.io.File


internal expect fun createClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient

class CoffeePeekClient(
    url: String,
    cacheFolder: File,
    private val debug: Boolean,
    getToken: () -> AuthResp?,
    saveToken: (AuthResp?) -> Unit
) {

    private val refreshClient = createClient {
        install(ContentNegotiation) { json(json = JsonExt.json) }
        defaultRequest {
            url(url)
            getToken()?.let {
                header(HttpHeaders.Authorization, "${AuthScheme.Bearer} ${it.accessToken}")
            }
        }
    }.also { intercept(it) }

    private val authService = AuthService(refreshClient)

    val client = createClient {
        install(ContentNegotiation) { json(json = JsonExt.json) }
        defaultRequest {
            url(url)
            getToken()?.let {
                header(HttpHeaders.Authorization, "${AuthScheme.Bearer} ${it.accessToken}")
            }
        }
        install(Auth) {
            bearer {
                loadTokens {
                    return@loadTokens getToken()?.let { BearerTokens(it.accessToken, it.refreshToken) }
                    val pairs = authService.createUser().getOrThrow()
                    saveToken(pairs)
                    BearerTokens(pairs.accessToken, pairs.refreshToken)
                }
                refreshTokens {
                    var tokens = getToken()
                    if (tokens == null) tokens = authService.createUser().getOrThrow()
                    tokens = authService.authRefresh(tokens.refreshToken).getOrThrow()
                    saveToken(tokens)
                    BearerTokens(tokens.accessToken, tokens.refreshToken)
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
            execute(it).also {
                val resp = it.response.bodyAsText(Charsets.UTF_8)
                println("CURL ${it.response.status.value}")
            }
        }
    }


}