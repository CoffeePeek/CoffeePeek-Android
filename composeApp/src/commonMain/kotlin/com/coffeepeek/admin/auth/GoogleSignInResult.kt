package com.coffeepeek.admin.auth

import com.coffeepeek.admin.utils.ErrorHandler

internal const val GOOGLE_SIGN_IN_CANCELLED = "Вход через Google отменён"

fun handleGoogleSignInResult(
    result: Result<String>,
    onSuccess: (String) -> Unit,
    onFailure: ((String) -> Unit)? = null,
) {
    result.fold(
        onSuccess = onSuccess,
        onFailure = { error ->
            if (error.message == GOOGLE_SIGN_IN_CANCELLED) return
            val message = error.message?.takeIf { it.isNotBlank() }
                ?: "Не удалось войти через Google"
            onFailure?.invoke(message) ?: ErrorHandler.showError(message)
        },
    )
}
