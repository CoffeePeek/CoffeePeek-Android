package com.coffeepeek.api.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class AccountDeletionRequestDto(
    @SerialName("requestId") @JsonNames("RequestId") val requestId: String = "",
    @SerialName("status") @JsonNames("Status") val status: String = "",
    @SerialName("expiresAtUtc") @JsonNames("ExpiresAtUtc") val expiresAtUtc: String = "",
    @SerialName("resendAvailableAtUtc") @JsonNames("ResendAvailableAtUtc") val resendAvailableAtUtc: String = "",
)
