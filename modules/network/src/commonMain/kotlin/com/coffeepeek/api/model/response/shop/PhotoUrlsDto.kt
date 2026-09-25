package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** imgproxy variants; without the proxy the backend fills all four with fullUrl. */
@Serializable
data class PhotoUrlsDto(
    @SerialName("thumbnail") val thumbnail: String? = null, // 240×180 crop
    @SerialName("card") val card: String? = null, // 600×450 crop
    @SerialName("detail") val detail: String? = null, // fit 1200
    @SerialName("fullscreen") val fullscreen: String? = null, // fit 1920
)

/** Picked variant, else [fullUrl]; null when both are blank. */
fun PhotoUrlsDto?.variantOr(fullUrl: String?, pick: (PhotoUrlsDto) -> String?): String? =
    this?.let(pick)?.takeIf { it.isNotBlank() } ?: fullUrl?.takeIf { it.isNotBlank() }
