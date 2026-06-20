package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatalogItemDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
)
