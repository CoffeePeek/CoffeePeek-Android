package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun getSession(): Session?
    suspend fun saveSession(session: Session?)
    fun observeSession(): Flow<Session?>
    suspend fun isLoggedIn(): Boolean
}
