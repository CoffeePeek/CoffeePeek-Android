package com.coffeepeek.api.model.response

import com.coffeepeek.api.model.DataResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CodeResp(
    @SerialName("secret") val secret: String,
    @SerialName("bot_url") val botUrl: String
): DataResponse()
