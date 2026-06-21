package com.coffeepeek.api.model.response.shop

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoffeeShopDetailsDto(
    @SerialName("id") val id: String,
    @SerialName("cityId") val cityId: String = "",
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("photos") val photos: List<ShortPhotoDto> = emptyList(),
    @SerialName("rating") val rating: Double = 0.0,
    @SerialName("reviewCount") val reviewCount: Int = 0,
    @SerialName("reviews") val reviews: List<ReviewDto> = emptyList(),
    @SerialName("isFavorite") val isFavorite: Boolean = false,
    @SerialName("isVisited") val isVisited: Boolean = false,
    @SerialName("canCreateReview") val canCreateReview: Boolean? = null,
    @SerialName("existingReviewId") val existingReviewId: String? = null,
    @SerialName("isOpen") val isOpen: Boolean = false,
    @SerialName("isNew") val isNew: Boolean = false,
    @SerialName("priceRange") val priceRange: Int = 1,
    @SerialName("location") val location: LocationDto? = null,
    @SerialName("coffeeBeans") val coffeeBeans: List<CatalogItemDto> = emptyList(),
    @SerialName("roasters") val roasters: List<CatalogItemDto> = emptyList(),
    @SerialName("equipments") val equipments: List<CatalogItemDto> = emptyList(),
    @SerialName("brewMethods") val brewMethods: List<CatalogItemDto> = emptyList(),
    @SerialName("shopContact") val shopContact: ShopContactDto? = null,
    @SerialName("schedules") val schedules: List<ScheduleDto>? = null,
)

@Serializable
data class ShopContactDto(
    @SerialName("instagramLink") val instagramLink: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("siteLink") val siteLink: String? = null,
    @SerialName("phoneNumber") val phoneNumber: String? = null,
)

@Serializable
data class GetShopDetailsResponseDto(
    @SerialName("shopDto") val shopDto: CoffeeShopDetailsDto,
) : DataResponse()
