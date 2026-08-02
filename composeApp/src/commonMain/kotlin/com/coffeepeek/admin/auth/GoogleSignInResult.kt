package com.coffeepeek.admin.auth

import com.coffeepeek.admin.utils.ErrorHandler

private const val GOOGLE_SIGN_IN_CANCELLED = "Вход через Google отменён"

fun handleGoogleSignInResult(
    result: Result<String>,
    onSuccess: (String) -> Unit,
) {
    result.fold(
        onSuccess = onSuccess,
        onFailure = { error ->
            if (error.message == GOOGLE_SIGN_IN_CANCELLED) return
            ErrorHandler.showError(
                error.message?.takeIf { it.isNotBlank() } ?: "Не удалось войти через Google",
            )
        },
    )
}
