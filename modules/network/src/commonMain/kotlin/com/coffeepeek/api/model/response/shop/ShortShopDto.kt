package com.coffeepeek.api.model.response.shop

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShortShopDto(
    @SerialName("id") val id: String,
    @SerialName("cityId") val cityId: String = "",
    @SerialName("name") val name: String,
    @SerialName("photos") val photos: List<ShortPhotoDto> = emptyList(),
    @SerialName("rating") val rating: Double = 0.0,
    @SerialName("reviewCount") val reviewCount: Int = 0,
    @SerialName("isFavorite") val isFavorite: Boolean = false,
    @SerialName("isVisited") val isVisited: Boolean = false,
    @SerialName("isNew") val isNew: Boolean = false,
    @SerialName("isOpen") val isOpen: Boolean = false,
    @SerialName("priceRange") val priceRange: Int = 1,
    @SerialName("location") val location: LocationDto? = null,
    @SerialName("beans") val beans: List<CatalogItemDto> = emptyList(),
    @SerialName("roasters") val roasters: List<CatalogItemDto> = emptyList(),
    @SerialName("equipments") val equipments: List<CatalogItemDto> = emptyList(),
    @SerialName("brewMethods") val brewMethods: List<CatalogItemDto> = emptyList(),
)

@Serializable
data class GetShopsResponseDto(
    @SerialName("coffeeShops") val coffeeShops: List<ShortShopDto> = emptyList(),
    @SerialName("currentPage") val currentPage: Int = 1,
    @SerialName("pageSize") val pageSize: Int = 10,
    @SerialName("totalItems") val totalItems: Int = 0,
    @SerialName("totalPages") val totalPages: Int = 0,
) : DataResponse()
