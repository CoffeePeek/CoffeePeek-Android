package com.coffeepeek.data.repository

import com.coffeepeek.api.service.AuthService
import com.coffeepeek.domain.model.Session
import com.coffeepeek.domain.repository.AuthRepository
import com.coffeepeek.domain.repository.SessionRepository

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val sessionRepository: SessionRepository,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Session> =
        authService.login(email, password).map { response ->
            Session(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
            ).also { sessionRepository.saveSession(it) }
        }

    override suspend fun register(userName: String, email: String, password: String): Result<Unit> =
        authService.register(userName, email, password)

    override suspend fun isEmailTaken(email: String): Result<Boolean> =
        authService.isEmailTaken(email)

    override suspend fun logout(): Result<Unit> {
        val refreshToken = sessionRepository.getSession()?.refreshToken
        return authService.logout(refreshToken).onSuccess { sessionRepository.saveSession(null) }
    }
}
