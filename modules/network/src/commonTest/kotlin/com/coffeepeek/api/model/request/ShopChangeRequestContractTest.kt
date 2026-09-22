package com.coffeepeek.api.model.request

import com.coffeepeek.api.utils.JsonExt
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ShopChangeRequestContractTest {

    private val json = JsonExt.json

    @Test
    fun createDescriptionBodyUsesSectionAndPayloadKeys() {
        val encoded = json.encodeToString(
            CreateShopChangeRequestBody.serializer(),
            CreateShopChangeRequestBody(
                shopId = "611c3b59-c086-44bb-a4c5-c350be4ee7d9",
                section = ShopChangeSectionDto.Description,
                payload = ShopChangePayloadDto(description = "Новое описание кофейни"),
            ),
        )
        assertContains(encoded, "\"shopId\":\"611c3b59-c086-44bb-a4c5-c350be4ee7d9\"")
        assertContains(encoded, "\"section\":\"Description\"")
        assertContains(encoded, "\"description\":\"Новое описание кофейни\"")
    }

    @Test
    fun contactsPayloadKeepsNullFieldsToClearValues() {
        val encoded = json.encodeToString(
            ShopChangePayloadDto.serializer(),
            ShopChangePayloadDto(
                contacts = ShopChangeContactsDto(
                    phoneNumber = "+375291234567",
                    email = null,
                    siteLink = null,
                    instagramLink = "@coffee",
                ),
            ),
        )
        assertContains(encoded, "\"phoneNumber\":\"+375291234567\"")
        assertContains(encoded, "\"email\":null")
        assertContains(encoded, "\"instagramLink\":\"@coffee\"")
    }

    @Test
    fun requestDtoDecodesCamelCaseEnums() {
        val decoded = json.decodeFromString(
            ShopChangeRequestDto.serializer(),
            """
            {
              "id": "req-1",
              "shopId": "shop-1",
              "submittedByUserId": "user-1",
              "section": "Tags",
              "payload": { "tagIds": ["tag-1"] },
              "status": "Pending",
              "reviewedByUserId": null,
              "reviewedAtUtc": null,
              "rejectionReason": null,
              "createdAtUtc": "2026-09-22T08:00:00Z",
              "updatedAtUtc": null
            }
            """.trimIndent(),
        )
        assertEquals(ShopChangeSectionDto.Tags, decoded.section)
        assertEquals(ModerationStatusDto.Pending, decoded.status)
        assertEquals(listOf("tag-1"), decoded.payload.tagIds)
    }
}
