package com.coffeepeek.admin.auth

expect object GoogleAuth {
    fun isSupported(): Boolean
    suspend fun signIn(): Result<String>
}
