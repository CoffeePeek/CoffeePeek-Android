package com.coffeepeek.api.model.response.shop

import com.coffeepeek.api.serialization.FlexibleIntSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShortPhotoDto(
    @SerialName("id") val id: String = "",
    @SerialName("fileName") val fileName: String? = null,
    @SerialName("storageKey") val storageKey: String? = null,
    @SerialName("fullUrl") val fullUrl: String? = null,
    @SerialName("sortIndex")
    @Serializable(with = FlexibleIntSerializer::class)
    val sortIndex: Int = 0,
    @SerialName("isPrimary") val isPrimary: Boolean = false,
)
