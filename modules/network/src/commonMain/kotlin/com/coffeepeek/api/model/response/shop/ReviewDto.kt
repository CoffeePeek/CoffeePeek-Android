package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    @SerialName("id") val id: String,
    @SerialName("userId") val userId: String,
    @SerialName("coffeeShopId") val coffeeShopId: String,
    @SerialName("username") val username: String = "",
    @SerialName("header") val header: String = "",
    @SerialName("comment") val comment: String = "",
    @SerialName("rating") val rating: RatingDto = RatingDto(),
    @SerialName("createdAtUtc") val createdAtUtc: String = "",
)
