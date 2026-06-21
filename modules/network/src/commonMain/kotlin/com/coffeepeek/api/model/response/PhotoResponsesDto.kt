package com.coffeepeek.api.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerateUploadUrlDto(
    @SerialName("photoId") val photoId: String,
    @SerialName("uploadUrl") val uploadUrl: String,
    @SerialName("storageKey") val storageKey: String,
)
