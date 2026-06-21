package com.coffeepeek.api.model.request

import com.coffeepeek.api.model.response.shop.RatingDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendReviewReq(
    @SerialName("shopId") val shopId: String,
    @SerialName("header") val header: String,
    @SerialName("comment") val comment: String,
    @SerialName("rating") val rating: RatingDto,
    @SerialName("photos") val photos: List<UploadedPhotoReq>? = null,
)

@Serializable
data class UpdateReviewReq(
    @SerialName("header") val header: String,
    @SerialName("comment") val comment: String,
    @SerialName("rating") val rating: RatingDto,
    @SerialName("photos") val photos: List<UploadedPhotoReq>? = null,
)
