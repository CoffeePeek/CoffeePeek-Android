package com.coffeepeek.api

import kotlin.test.Test
import kotlin.test.assertEquals

class CoffeePeekClientTest {

    @Test
    fun removesTrailingSlashFromBaseUrl() {
        // Given
        val url = "https://api.coffeepeek.by/"

        // When
        val normalizedUrl = normalizeBaseUrl(url)

        // Then
        assertEquals("https://api.coffeepeek.by", normalizedUrl)
    }

    @Test
    fun removesWhitespaceAndRepeatedTrailingSlashes() {
        // Given
        val url = "  https://api.coffeepeek.by///  "

        // When
        val normalizedUrl = normalizeBaseUrl(url)

        // Then
        assertEquals("https://api.coffeepeek.by", normalizedUrl)
    }
}
