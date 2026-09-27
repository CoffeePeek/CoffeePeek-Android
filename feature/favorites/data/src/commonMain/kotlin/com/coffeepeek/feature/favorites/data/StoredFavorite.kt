package com.coffeepeek.feature.favorites.data

import com.coffeepeek.feature.favorites.domain.FavoriteShop
import kotlinx.serialization.Serializable

/** Field names/defaults intentionally match legacy LocalFavoriteShopDto. Never exported. */
@Serializable
internal data class StoredFavorite(
    val id: String,
    val title: String,
    val rating: Double? = null,
    val reviewCount: Int = 0,
    val cityName: String? = null,
    val priceRange: String? = null,
    val photoUrl: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isOpen: Boolean = false,
    val tags: List<String> = emptyList(),
    val brewMethods: List<String> = emptyList(),
    val roasterPhotoUrl: String? = null,
    val roasterPhotoUrls: List<String> = emptyList(),
) {
    fun toDomain() = FavoriteShop(id, title, rating, reviewCount, cityName, priceRange,
        photoUrl, address, latitude, longitude, isOpen, tags.toList(), brewMethods.toList(),
        roasterPhotoUrls.ifEmpty { listOfNotNull(roasterPhotoUrl) }.toList())
}

internal fun FavoriteShop.toStored() = StoredFavorite(id, title, rating, reviewCount,
    cityName, priceRange, photoUrl, address, latitude, longitude, isOpen, tags.toList(),
    brewMethods.toList(), roasterPhotoUrls.firstOrNull(), roasterPhotoUrls.toList())
