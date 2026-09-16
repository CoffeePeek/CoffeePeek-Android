package com.coffeepeek.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.cache.storage.CacheStorage

internal actual fun createClient(block: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Darwin) { block() }

internal actual fun createUploadClient(block: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Darwin) { block() }

// Ktor's persistent FileStorage is JVM-only. The default in-memory cache is used on iOS;
// URL responses and image data are still cached by NSURLSession and Kamel at platform level.
internal actual fun createHttpCacheStorage(cacheFolderPath: String): CacheStorage? = null
