package com.coffeepeek.api.model.response.shop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoasterDetailsDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("about") val about: String? = null,
    @SerialName("location") val location: RoasterLocationDto? = null,
    @SerialName("contact") val contact: RoasterContactDto? = null,
    @SerialName("photos") val photos: List<RoasterPhotoDto> = emptyList(),
    @SerialName("shops") val shops: List<RoasterShopDto> = emptyList(),
)

@Serializable
data class RoasterLocationDto(
    @SerialName("address") val address: String = "",
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
)

@Serializable
data class RoasterContactDto(
    @SerialName("instagramLink") val instagramLink: String? = null,
    @SerialName("siteLink") val siteLink: String? = null,
)

@Serializable
data class RoasterPhotoDto(
    @SerialName("id") val id: String = "",
    @SerialName("fileName") val fileName: String = "",
    @SerialName("storageKey") val storageKey: String = "",
    @SerialName("fullUrl") val fullUrl: String? = null,
    @SerialName("sortIndex") val sortIndex: Int = 0,
)

@Serializable
data class RoasterShopDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
)

@Serializable
data class RoasterSubmissionDto(
    @SerialName("roasterId") val roasterId: String,
    @SerialName("status") val status: String,
    @SerialName("isAddressValidated") val isAddressValidated: Boolean = false,
)

data class RoasterSubmissionApiResult(
    val data: RoasterSubmissionDto,
    val message: String,
)
