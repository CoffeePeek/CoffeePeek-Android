package com.coffeepeek.api.model.response

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleLoginResp(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("user") val user: GoogleLoginUserDto,
) : DataResponse()

@Serializable
data class GoogleLoginUserDto(
    @SerialName("email") val email: String,
    @SerialName("username") val username: String,
    @SerialName("avatarUrl") val avatarUrl: String = "",
)
