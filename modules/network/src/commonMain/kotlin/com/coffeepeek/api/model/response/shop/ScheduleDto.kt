package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ScheduleDto(
    @SerialName("dayOfWeek") val dayOfWeek: JsonElement? = null,
    @SerialName("isClosed") val isClosed: Boolean = false,
    @SerialName("intervals") val intervals: List<ShopScheduleIntervalDto>? = null,
)

@Serializable
data class ShopScheduleIntervalDto(
    @SerialName("openTime") val openTime: String = "",
    @SerialName("closeTime") val closeTime: String = "",
)
