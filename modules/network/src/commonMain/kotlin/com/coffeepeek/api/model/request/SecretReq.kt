package com.coffeepeek.api.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SecretReq(
    @SerialName("secret") val secret: String,
)
