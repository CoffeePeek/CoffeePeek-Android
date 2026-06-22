package com.coffeepeek.api.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUsernameReq(@SerialName("username") val username: String)

@Serializable
data class UpdateAboutReq(@SerialName("about") val about: String)

@Serializable
data class UpdateAvatarReq(
    @SerialName("uploadedPhoto") val uploadedPhoto: UploadedPhotoReq,
)
