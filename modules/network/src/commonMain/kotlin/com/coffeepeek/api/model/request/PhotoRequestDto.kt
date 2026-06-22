package com.coffeepeek.api.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhotoRequestDto(
    @SerialName("sizeBytes") val sizeBytes: Int,
    @SerialName("fileName") val fileName: String,
    @SerialName("contentType") val contentType: String,
)

@Serializable
data class UploadedPhotoReq(
    @SerialName("fileName") val fileName: String,
    @SerialName("contentType") val contentType: String,
    @SerialName("storageKey") val storageKey: String,
    @SerialName("size") val size: Long,
)

@Serializable
data class ScheduleReq(
    @SerialName("dayOfWeek") val dayOfWeek: Int,
    @SerialName("isClosed") val isClosed: Boolean,
    @SerialName("intervals") val intervals: List<ScheduleIntervalReq>? = null,
)

@Serializable
data class ScheduleIntervalReq(
    @SerialName("openTime") val openTime: String,
    @SerialName("closeTime") val closeTime: String,
)
