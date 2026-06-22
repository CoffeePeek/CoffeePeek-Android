package com.coffeepeek.admin.auth

actual object GoogleAuth {
    actual fun isSupported(): Boolean = false
    actual suspend fun signIn(): Result<String> =
        Result.failure(UnsupportedOperationException("Google Sign-In недоступен на Desktop"))
}
