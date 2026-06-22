package com.coffeepeek.api.model.response

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResp(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String = "",
): DataResponse()