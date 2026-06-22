package com.coffeepeek.data.repository

import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.domain.model.Session
import com.coffeepeek.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.example.project.DatabaseCore
import org.example.project.repository.readSerializable
import org.example.project.repository.readSerializableFlow
import org.example.project.repository.saveSerializable

class SessionRepositoryImpl(
    database: DatabaseCore,
) : SessionRepository {

    companion object {
        private const val SESSION_KEY = "SESSION"
        private const val LEGACY_SESSION_KEY = "API_KEYS"
    }

    private val settings = database.settingRepository

    init {
        runBlocking { migrateLegacySessionIfNeeded() }
    }

    private suspend fun migrateLegacySessionIfNeeded() {
        if (getSession() != null) return
        val legacy = settings.readSerializable<AuthResp>(LEGACY_SESSION_KEY) ?: return
        saveSession(legacy.toSession())
        settings.delete(LEGACY_SESSION_KEY)
    }

    override suspend fun getSession(): Session? =
        settings.readSerializable<AuthResp>(SESSION_KEY)?.toSession()

    override suspend fun saveSession(session: Session?) {
        settings.saveSerializable(SESSION_KEY, session?.toAuthResp())
    }

    override fun observeSession(): Flow<Session?> =
        settings.readSerializableFlow<AuthResp>(SESSION_KEY).map { it?.toSession() }

    override suspend fun isLoggedIn(): Boolean = getSession()?.accessToken?.isNotBlank() == true

    private fun AuthResp.toSession() = Session(accessToken, refreshToken)
    private fun Session.toAuthResp() = AuthResp(accessToken, refreshToken.orEmpty())
}
