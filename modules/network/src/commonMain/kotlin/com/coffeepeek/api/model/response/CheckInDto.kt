package com.coffeepeek.api.model.response

import com.coffeepeek.api.model.DataResponse
import com.coffeepeek.api.model.response.shop.ShortPhotoDto
import com.coffeepeek.api.model.response.shop.RatingDto
import com.coffeepeek.api.serialization.FlexibleIntSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInDto(
    @SerialName("id") val id: String,
    @SerialName("userId") val userId: String,
    @SerialName("shopId") val shopId: String,
    @SerialName("note") val note: String? = null,
    @SerialName("createdAt") val createdAt: String = "",
    @SerialName("visitedAt") val visitedAt: String = "",
    @SerialName("reviewId") val reviewId: String? = null,
    @SerialName("shopName") val shopName: String? = null,
    @SerialName("photos") val photos: List<ShortPhotoDto> = emptyList(),
    @SerialName("rating") val rating: RatingDto? = null,
)

@Serializable
data class GetUserCheckInsResponseDto(
    @SerialName("checkIns") val checkIns: List<CheckInDto> = emptyList(),
    @SerialName("totalItems")
    @Serializable(with = FlexibleIntSerializer::class)
    val totalItems: Int = 0,
    @SerialName("totalPages")
    @Serializable(with = FlexibleIntSerializer::class)
    val totalPages: Int = 0,
    @SerialName("currentPage")
    @Serializable(with = FlexibleIntSerializer::class)
    val currentPage: Int = 1,
    @SerialName("pageSize")
    @Serializable(with = FlexibleIntSerializer::class)
    val pageSize: Int = 10,
) : DataResponse()

@Serializable
data class CreateCheckInResponseDto(
    @SerialName("checkInId") val checkInId: String? = null,
    @SerialName("reviewId") val reviewId: String? = null,
) : DataResponse()
