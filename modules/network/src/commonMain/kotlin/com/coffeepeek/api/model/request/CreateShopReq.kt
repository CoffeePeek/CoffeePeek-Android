package com.coffeepeek.api.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateShopReq(
    @SerialName("name")          val name: String,
    @SerialName("address")       val address: String,
    @SerialName("cityId")        val cityId: String,
    @SerialName("description")   val description: String?      = null,
    @SerialName("priceRange")    val priceRange: Int?          = null,
    @SerialName("shopContact")   val shopContact: CreateShopContactReq? = null,
    @SerialName("equipmentIds")  val equipmentIds: List<String>? = null,
    @SerialName("coffeeBeanIds") val coffeeBeanIds: List<String>? = null,
    @SerialName("roasterIds")    val roasterIds: List<String>? = null,
    @SerialName("brewMethodIds") val brewMethodIds: List<String>? = null,
)

@Serializable
data class CreateShopContactReq(
    @SerialName("phoneNumber")   val phoneNumber: String?   = null,
    @SerialName("email")         val email: String?         = null,
    @SerialName("siteLink")      val siteLink: String?      = null,
    @SerialName("instagramLink") val instagramLink: String? = null,
)
