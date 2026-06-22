package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadedPhotoDto(
    @SerialName("fileName") val fileName: String = "",
    @SerialName("contentType") val contentType: String = "",
    @SerialName("storageKey") val storageKey: String = "",
    @SerialName("size") val size: Long = 0,
)
