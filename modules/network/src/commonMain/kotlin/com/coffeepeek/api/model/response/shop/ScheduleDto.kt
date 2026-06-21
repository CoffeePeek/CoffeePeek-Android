package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleDto(
    @SerialName("dayOfWeek") val dayOfWeek: Int = 0,
    @SerialName("isClosed") val isClosed: Boolean = false,
    @SerialName("intervals") val intervals: List<ShopScheduleIntervalDto>? = null,
)

@Serializable
data class ShopScheduleIntervalDto(
    @SerialName("openTime") val openTime: String = "",
    @SerialName("closeTime") val closeTime: String = "",
)
