package com.coffeepeek.api.model.response.shop

import com.coffeepeek.api.serialization.FlexibleIntSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatingDto(
    @SerialName("place") @Serializable(with = FlexibleIntSerializer::class) val place: Int = 0,
    @SerialName("service") @Serializable(with = FlexibleIntSerializer::class) val service: Int = 0,
    @SerialName("coffee") @Serializable(with = FlexibleIntSerializer::class) val coffee: Int = 0,
)
