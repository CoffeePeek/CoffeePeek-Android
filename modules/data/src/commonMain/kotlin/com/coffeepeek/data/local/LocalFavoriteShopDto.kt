package com.coffeepeek.data.local

import com.coffeepeek.domain.model.CoffeeShop
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.ShopLocation
import kotlinx.serialization.Serializable

@Serializable
data class LocalFavoriteShopDto(
    val id: String,
    val title: String,
    val rating: Double? = null,
    val reviewCount: Int = 0,
    val cityName: String? = null,
    val priceRange: String? = null,
    val photoUrl: String? = null,
    val address: String? = null,
    val isOpen: Boolean = false,
    val tags: List<String> = emptyList(),
) {
    fun toDomain(): CoffeeShopDetails = CoffeeShopDetails(
        shop = CoffeeShop(
            id = id,
            title = title,
            rating = rating,
            reviewCount = reviewCount,
            cityName = cityName,
            priceRange = priceRange,
            photoUrl = photoUrl,
            address = address,
            isOpen = isOpen,
            isFavorite = true,
            tags = tags,
        ),
        location = address?.let { ShopLocation(address = it) },
    )

    companion object {
        fun from(shop: CoffeeShop, address: String? = null) = LocalFavoriteShopDto(
            id = shop.id,
            title = shop.title,
            rating = shop.rating,
            reviewCount = shop.reviewCount,
            cityName = shop.cityName,
            priceRange = shop.priceRange,
            photoUrl = shop.photoUrl,
            address = address ?: shop.address,
            isOpen = shop.isOpen,
            tags = shop.tags,
        )
    }
}
