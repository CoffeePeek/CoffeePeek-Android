package com.coffeepeek.api.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateItemReq(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("image_ids") val imageIds: List<String>
)