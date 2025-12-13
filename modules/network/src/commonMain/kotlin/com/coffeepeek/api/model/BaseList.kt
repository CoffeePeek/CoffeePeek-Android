package com.coffeepeek.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseList<T: DataResponse>(
   @SerialName("list") val list: List<T>
): DataResponse()