package com.coffeepeek.api.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemsReq(
    @SerialName("name") val name: String? = null,
    @SerialName("page") val page: Long = 0
)