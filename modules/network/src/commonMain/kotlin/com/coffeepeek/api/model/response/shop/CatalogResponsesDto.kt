package com.coffeepeek.api.model.response.shop

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityItemDto(
    @SerialName("id")   val id: String,
    @SerialName("name") val name: String,
)

@Serializable
data class GetCitiesResponseDto(
    @SerialName("cities") val cities: List<CityItemDto> = emptyList(),
) : DataResponse()

@Serializable
data class GetCatalogItemsResponseDto(
    @SerialName("items") val items: List<CatalogItemDto> = emptyList(),
) : DataResponse()

// Individual response wrappers returned by each catalog endpoint

@Serializable
data class GetBeansResponseDto(
    @SerialName("beans") val beans: List<CatalogItemDto> = emptyList(),
) : DataResponse()

@Serializable
data class GetEquipmentResponseDto(
    @SerialName("equipments") val equipments: List<CatalogItemDto> = emptyList(),
) : DataResponse()

@Serializable
data class GetRoastersResponseDto(
    @SerialName("roasters") val roasters: List<CatalogItemDto> = emptyList(),
) : DataResponse()

@Serializable
data class GetBrewMethodsResponseDto(
    @SerialName("brewMethods") val brewMethods: List<CatalogItemDto> = emptyList(),
) : DataResponse()
