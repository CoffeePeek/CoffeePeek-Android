package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatingDto(
    @SerialName("place") val place: Int = 0,
    @SerialName("service") val service: Int = 0,
    @SerialName("coffee") val coffee: Int = 0,
)
