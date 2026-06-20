package com.coffeepeek.domain.model

data class CoffeeShop(
    val id: String,
    val title: String,
    val rating: Double?,
    val reviewCount: Int = 0,
    val cityName: String?,
    val priceRange: String?,
    val photoUrl: String?,
    val address: String? = null,
    val isOpen: Boolean = false,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
)

data class CoffeeShopDetails(
    val shop: CoffeeShop,
    val description: String?,
    val address: String?,
    val isVisited: Boolean = false,
    val canCreateReview: Boolean = false,
    val photos: List<String> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val contact: ShopContact? = null,
    val brewMethods: List<String> = emptyList(),
    val coffeeBeans: List<String> = emptyList(),
    val roasters: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
)

data class Review(
    val id: String,
    val username: String,
    val header: String,
    val comment: String,
    val rating: Double,
    val createdAt: String,
)

data class ShopContact(
    val instagram: String? = null,
    val email: String? = null,
    val website: String? = null,
    val phone: String? = null,
)

data class ShopFilters(
    val query: String? = null,
    val cityId: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class MapBounds(
    val minLat: Double,
    val minLon: Double,
    val maxLat: Double,
    val maxLon: Double,
)

data class MapShop(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
)

data class PagedResult<T>(
    val items: List<T>,
    val totalCount: Int,
    val totalPages: Int,
    val currentPage: Int,
)
