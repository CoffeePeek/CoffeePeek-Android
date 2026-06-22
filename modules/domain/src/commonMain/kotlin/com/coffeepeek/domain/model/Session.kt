package com.coffeepeek.domain.model

data class Session(
    val accessToken: String,
    val refreshToken: String? = null,
    val userId: String? = null,
)
