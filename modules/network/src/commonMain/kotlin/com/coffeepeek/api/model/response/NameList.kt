package com.coffeepeek.api.model.response

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NameList(
    @SerialName("name_list") val nameList: List<String>
): DataResponse()