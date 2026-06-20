package com.coffeepeek.api.model.response

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    @SerialName("userName")       val userName: String,
    @SerialName("email")          val email: String,
    @SerialName("about")          val about: String? = null,
    @SerialName("avatarUrl")      val avatarUrl: String? = null,
    @SerialName("reviewCount")    val reviewCount: Int = 0,
    @SerialName("checkInCount")   val checkInCount: Int = 0,
    @SerialName("addedShopsCount") val addedShopsCount: Int = 0,
) : DataResponse()
