package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShortPhotoDto(
    @SerialName("fileName") val fileName: String = "",
    @SerialName("storageKey") val storageKey: String = "",
    @SerialName("fullUrl") val fullUrl: String? = null,
)
