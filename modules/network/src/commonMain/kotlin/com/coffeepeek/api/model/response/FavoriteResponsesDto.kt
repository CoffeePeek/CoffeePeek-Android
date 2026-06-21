package com.coffeepeek.api.model.response

import com.coffeepeek.api.model.DataResponse
import com.coffeepeek.api.model.response.shop.CoffeeShopDetailsDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAllFavoritesResponseDto(
    @SerialName("favoriteShops") val favoriteShops: List<CoffeeShopDetailsDto> = emptyList(),
) : DataResponse()
