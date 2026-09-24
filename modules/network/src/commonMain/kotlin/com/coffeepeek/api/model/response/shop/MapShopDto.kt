@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.coffeepeek.api.model.response.shop

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames

@Serializable
data class MapShopDto(
    @SerialName("id") @JsonNames("Id") val id: String,
    @SerialName("latitude") @JsonNames("Latitude") val latitude: Double,
    @SerialName("longitude") @JsonNames("Longitude") val longitude: Double,
    @SerialName("title") @JsonNames("Title") val title: String? = null,
    @SerialName("type") @JsonNames("Type") val type: JsonElement? = null,
    @SerialName("primaryZoneId") @JsonNames("PrimaryZoneId") val primaryZoneId: String? = null,
)

@Serializable
data class GetShopsInBoundsResponseDto(
    @SerialName("shops") @JsonNames("Shops") val shops: List<MapShopDto> = emptyList(),
    @SerialName("clusters") @JsonNames("Clusters") val clusters: List<MapClusterDto> = emptyList(),
    @SerialName("zones") @JsonNames("Zones") val zones: List<MapCoffeeZoneDto> = emptyList(),
    @SerialName("isTruncated") @JsonNames("IsTruncated") val isTruncated: Boolean = false,
) : DataResponse()

@Serializable
data class MapClusterDto(
    @SerialName("id") @JsonNames("Id") val id: String? = null,
    @SerialName("latitude") @JsonNames("Latitude") val latitude: Double,
    @SerialName("longitude") @JsonNames("Longitude") val longitude: Double,
    @SerialName("count") @JsonNames("Count") val count: Int,
    @SerialName("bounds") @JsonNames("Bounds") val bounds: MapClusterBoundsDto,
)

@Serializable
data class MapClusterBoundsDto(
    @SerialName("minLatitude") @JsonNames("MinLatitude") val minLatitude: Double,
    @SerialName("minLongitude") @JsonNames("MinLongitude") val minLongitude: Double,
    @SerialName("maxLatitude") @JsonNames("MaxLatitude") val maxLatitude: Double,
    @SerialName("maxLongitude") @JsonNames("MaxLongitude") val maxLongitude: Double,
)

@Serializable
data class MapCoffeeZoneDto(
    @SerialName("id") @JsonNames("Id") val id: String,
    @SerialName("name") @JsonNames("Name") val name: String = "",
    @SerialName("description") @JsonNames("Description") val description: String = "",
    @SerialName("latitude") @JsonNames("Latitude") val latitude: Double,
    @SerialName("longitude") @JsonNames("Longitude") val longitude: Double,
    @SerialName("radiusMeters") @JsonNames("RadiusMeters") val radiusMeters: Double,
    @SerialName("shopCount") @JsonNames("ShopCount") val shopCount: Int,
    @SerialName("polygon") @JsonNames("Polygon") val polygon: List<MapZonePointDto> = emptyList(),
)

@Serializable
data class MapZonePointDto(
    @SerialName("latitude") @JsonNames("Latitude") val latitude: Double,
    @SerialName("longitude") @JsonNames("Longitude") val longitude: Double,
)
