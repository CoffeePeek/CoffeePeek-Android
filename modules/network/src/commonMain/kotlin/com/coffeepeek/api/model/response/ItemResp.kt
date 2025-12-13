package com.coffeepeek.api.model.response

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemResp(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("creator") val creator: String,
    @SerialName("editor") val editor: String,
    @SerialName("images") val images: List<String>
): DataResponse()