package com.coffeepeek.domain.model

data class AccountDeletionRequest(
    val requestId: String,
    val status: String,
    val expiresAtUtc: String,
    val resendAvailableAtUtc: String,
)
