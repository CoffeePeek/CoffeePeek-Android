package com.coffeepeek.api.model.response

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInDto(
    @SerialName("id") val id: String,
    @SerialName("userId") val userId: String,
    @SerialName("shopId") val shopId: String,
    @SerialName("note") val note: String = "",
    @SerialName("createdAt") val createdAt: String = "",
    @SerialName("reviewId") val reviewId: String? = null,
    @SerialName("shopName") val shopName: String = "",
)

@Serializable
data class GetUserCheckInsResponseDto(
    @SerialName("checkIns") val checkIns: List<CheckInDto> = emptyList(),
    @SerialName("totalItems") val totalItems: Int = 0,
    @SerialName("totalPages") val totalPages: Int = 0,
    @SerialName("currentPage") val currentPage: Int = 1,
    @SerialName("pageSize") val pageSize: Int = 10,
) : DataResponse()

@Serializable
data class CreateCheckInResponseDto(
    @SerialName("checkInId") val checkInId: String? = null,
    @SerialName("reviewId") val reviewId: String? = null,
) : DataResponse()
