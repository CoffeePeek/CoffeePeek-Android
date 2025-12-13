package com.coffeepeek.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    @SerialName("isSuccess") val isSuccess: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: T? = null
)

