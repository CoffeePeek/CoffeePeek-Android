package com.coffeepeek.data.repository

import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.data.util.JwtUtils
import com.coffeepeek.data.util.SessionAuth
import com.coffeepeek.domain.model.Session
import com.coffeepeek.domain.repository.SessionRepository
import com.coffeepeek.room.DatabaseCore
import com.coffeepeek.room.repository.readSerializable
import com.coffeepeek.room.repository.readSerializableFlow
import com.coffeepeek.room.repository.saveSerializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class SessionRepositoryImpl(
    database: DatabaseCore,
) : SessionRepository {

    companion object {
        private const val SESSION_KEY = "SESSION"
        private const val LEGACY_SESSION_KEY = "API_KEYS"
    }

    private val settings = database.settingRepository

    @Volatile
    private var memoryCache: Session? = null

    override fun peekSession(): Session? = memoryCache

    override fun applySession(session: Session?) {
        memoryCache = session
    }

    override fun isActiveSession(session: Session?): Boolean = SessionAuth.isActive(session)

    override suspend fun warmCache() {
        getSession()
    }

    private suspend fun migrateLegacySessionIfNeeded() {
        val existing = settings.readSerializable<AuthResp>(SESSION_KEY)
        if (existing != null) return
        val legacy = settings.readSerializable<AuthResp>(LEGACY_SESSION_KEY) ?: return
        saveSession(legacy.toSession())
        settings.delete(LEGACY_SESSION_KEY)
    }

    override suspend fun getSession(): Session? {
        migrateLegacySessionIfNeeded()
        return settings.readSerializable<AuthResp>(SESSION_KEY)?.toSession()?.also { memoryCache = it }
    }

    override suspend fun persistSession(session: Session?) {
        settings.saveSerializable(SESSION_KEY, session?.toAuthResp())
    }

    override suspend fun saveSession(session: Session?) {
        applySession(session)
        persistSession(session)
    }

    override fun observeSession(): Flow<Session?> = flow {
        migrateLegacySessionIfNeeded()
        emitAll(
            settings.readSerializableFlow<AuthResp>(SESSION_KEY).map { auth ->
                auth?.toSession().also { memoryCache = it }
            },
        )
    }

    override suspend fun isLoggedIn(): Boolean =
        isActiveSession(peekSession() ?: getSession())

    private fun AuthResp.toSession() = Session(
        accessToken = accessToken,
        refreshToken = refreshToken,
        userId = JwtUtils.extractUserId(accessToken),
    )

    private fun Session.toAuthResp() = AuthResp(accessToken, refreshToken.orEmpty())
}
