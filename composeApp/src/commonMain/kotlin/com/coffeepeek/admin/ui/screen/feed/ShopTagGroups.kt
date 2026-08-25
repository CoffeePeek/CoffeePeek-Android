package com.coffeepeek.admin.ui.screen.feed

import com.coffeepeek.domain.model.CatalogItem

/**
 * Splits shop-tag catalog into amenity tags (row 1) and venue-type tags (row 2).
 * Venue types: Specialty / Кофейня / Кафе — matched by slug or name.
 */
object ShopTagGroups {

    private val venueTypeOrder = listOf("specialty", "кофейня", "кафе", "cafe", "coffee-shop", "coffeeshop")

    private val venueTypeNames = setOf(
        "specialty",
        "кофейня",
        "кафе",
        "cafe",
        "coffee shop",
        "coffee-shop",
        "coffeeshop",
    )

    private val venueTypeSlugs = setOf(
        "specialty",
        "cafe",
        "coffee-shop",
        "coffeeshop",
        "kofeynya",
        "coffeehouse",
        "coffee-house",
    )

    fun CatalogItem.isVenueType(): Boolean {
        val nameKey = name.trim().lowercase()
        val slugKey = slug.trim().lowercase()
        return nameKey in venueTypeNames || slugKey in venueTypeSlugs
    }

    fun amenityTags(tags: List<CatalogItem>): List<CatalogItem> =
        tags.filterNot { it.isVenueType() }

    fun venueTypeTags(tags: List<CatalogItem>): List<CatalogItem> {
        val venue = tags.filter { it.isVenueType() }
        return venue.sortedBy { tag ->
            val key = tag.slug.trim().lowercase().ifBlank { tag.name.trim().lowercase() }
            val index = venueTypeOrder.indexOfFirst { key == it || key.contains(it) }
            if (index >= 0) index else Int.MAX_VALUE
        }
    }
}
