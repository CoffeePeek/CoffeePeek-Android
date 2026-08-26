package com.coffeepeek.data.util

class FileUrlResolver(private val baseUrl: String) {

    fun resolve(storageKey: String?, fullUrl: String? = null): String? {
        fullUrl?.trim()?.takeIf { it.isNotBlank() }?.let { return it }
        val key = storageKey?.trim().orEmpty()
        if (key.isBlank()) return null
        if (key.startsWith("http://") || key.startsWith("https://")) return key
        return "${baseUrl.trimEnd('/')}/api/file/$key"
    }
}
