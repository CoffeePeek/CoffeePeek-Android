package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PhotoUrlsDtoTest {

    @Test
    fun decodesUrlsAndFallsBackToFullUrl() {
        val photos = Json.decodeFromString<List<ShortPhotoDto>>(
            """
                [
                  { "fullUrl": "https://m/orig.jpg",
                    "urls": { "thumbnail": "https://m/thumb.jpg", "card": "", "detail": "https://m/detail.jpg" } },
                  { "fullUrl": "https://m/legacy.jpg" },
                  { }
                ]
            """.trimIndent(),
        )
        val (withUrls, legacy, empty) = photos

        assertEquals("https://m/thumb.jpg", withUrls.urls.variantOr(withUrls.fullUrl) { it.thumbnail })
        assertEquals("https://m/orig.jpg", withUrls.urls.variantOr(withUrls.fullUrl) { it.card }) // blank
        assertEquals("https://m/orig.jpg", withUrls.urls.variantOr(withUrls.fullUrl) { it.fullscreen }) // missing
        assertEquals("https://m/legacy.jpg", legacy.urls.variantOr(legacy.fullUrl) { it.detail }) // old backend
        assertNull(empty.urls.variantOr(empty.fullUrl) { it.card })
    }
}
