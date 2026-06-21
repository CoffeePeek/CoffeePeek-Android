package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.Session

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Session>
    suspend fun googleLogin(idToken: String): Result<Session>
    suspend fun register(userName: String, email: String, password: String): Result<Unit>
    suspend fun isEmailTaken(email: String): Result<Boolean>
    suspend fun logout(): Result<Unit>
}
