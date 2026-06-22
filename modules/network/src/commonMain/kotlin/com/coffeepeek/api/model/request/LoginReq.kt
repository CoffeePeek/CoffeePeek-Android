package com.coffeepeek.api.model.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginReq(
    @SerialName("email")
    val email: String?,
    @SerialName("password")
    val password: String?
)