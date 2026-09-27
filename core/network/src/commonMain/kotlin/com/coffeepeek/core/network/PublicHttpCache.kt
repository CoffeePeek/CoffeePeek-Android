package com.coffeepeek.core.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.Url

private val AnonymousCacheRequests = createClientPlugin("AnonymousCacheRequests") {
    on(SendingRequest) { request, _ ->
        require(!request.headers.contains(HttpHeaders.Authorization) && !request.headers.contains(HttpHeaders.Cookie)) {
            "Public cache client must not send session credentials"
        }
    }
}

/** Opt-in anonymous HTTP cache; storage capacity, persistence and cleanup belong to the caller. */
fun HttpClientConfig<*>.configurePublicHttpCache(storage: CacheStorage) {
    install(AnonymousCacheRequests)
    install(HttpCache) {
        isShared = true
        publicStorage(AnonymousCacheStorage(storage))
        privateStorage(CacheStorage.Disabled)
    }
}

private class AnonymousCacheStorage(private val delegate: CacheStorage) : CacheStorage by delegate {
    private fun CachedResponseData.isAnonymous(): Boolean = !headers.contains(HttpHeaders.SetCookie) &&
        varyKeys.keys.none { it.equals(HttpHeaders.Authorization, true) || it.equals(HttpHeaders.Cookie, true) }

    override suspend fun store(url: Url, data: CachedResponseData) {
        if (data.isAnonymous()) delegate.store(url, data)
    }

    override suspend fun find(url: Url, varyKeys: Map<String, String>): CachedResponseData? =
        delegate.find(url, varyKeys)?.takeIf { it.isAnonymous() }

    override suspend fun findAll(url: Url): Set<CachedResponseData> =
        delegate.findAll(url).filter { it.isAnonymous() }.toSet()
}
