package com.coffeepeek.api.model.response

import com.coffeepeek.api.model.DataResponse
import com.coffeepeek.api.model.response.shop.ReviewDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CanCreateReviewResponseDto(
    @SerialName("canCreate") val canCreate: Boolean = false,
    @SerialName("reviewId") val reviewId: String? = null,
) : DataResponse()

@Serializable
data class GetReviewsByUserIdResponseDto(
    @SerialName("reviewDtos") val reviewDtos: List<ReviewDto> = emptyList(),
    @SerialName("totalItems") val totalItems: Int = 0,
    @SerialName("totalPages") val totalPages: Int = 0,
    @SerialName("currentPage") val currentPage: Int = 1,
    @SerialName("pageSize") val pageSize: Int = 10,
) : DataResponse()

@Serializable
data class CreateEntityResponseDto(
    @SerialName("entityId") val entityId: String? = null,
) : DataResponse()
