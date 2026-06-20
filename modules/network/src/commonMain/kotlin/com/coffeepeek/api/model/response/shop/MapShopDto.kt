package com.coffeepeek.api.model.response.shop

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MapShopDto(
    @SerialName("id") val id: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("title") val title: String,
)

@Serializable
data class GetShopsInBoundsResponseDto(
    @SerialName("shops") val shops: List<MapShopDto> = emptyList(),
) : DataResponse()
