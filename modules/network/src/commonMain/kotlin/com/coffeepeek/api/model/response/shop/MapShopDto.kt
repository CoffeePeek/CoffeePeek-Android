package com.coffeepeek.api.model.response.shop

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MapShopDto(
    @SerialName("id") val id: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("title") val title: String? = null,
    @SerialName("type") val type: JsonElement? = null,
)

@Serializable
data class GetShopsInBoundsResponseDto(
    @SerialName("shops") val shops: List<MapShopDto> = emptyList(),
) : DataResponse()
