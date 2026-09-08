package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CoffeeShopDetailsDtoTest {

    @Test
    fun decodesUserCheckInsFromShopDetails() {
        val details = Json.decodeFromString<CoffeeShopDetailsDto>(
            """
                {
                  "id": "shop-1",
                  "name": "CoffeePeek",
                  "userCheckIns": [
                    {
                      "id": "check-in-1",
                      "userId": "user-1",
                      "shopId": "shop-1",
                      "note": "Отличный фильтр",
                      "createdAt": "2026-09-08T10:00:00Z",
                      "visitedAt": "2026-09-08T09:30:00Z",
                      "photos": [{ "fullUrl": "https://cdn.example/check-in.jpg" }]
                    }
                  ]
                }
            """.trimIndent(),
        )

        assertEquals(1, details.userCheckIns.size)
        assertEquals("2026-09-08T09:30:00Z", details.userCheckIns.single().visitedAt)
        assertEquals(
            "https://cdn.example/check-in.jpg",
            details.userCheckIns.single().photos.single().fullUrl,
        )
    }
}
