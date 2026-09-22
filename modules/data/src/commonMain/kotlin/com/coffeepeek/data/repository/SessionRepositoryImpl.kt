package com.coffeepeek.data.repository

import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.data.util.JwtUtils
import com.coffeepeek.data.util.SessionAuth
import com.coffeepeek.data.session.SessionSecureStore
import com.coffeepeek.domain.model.Session
import com.coffeepeek.domain.repository.SessionRepository
import com.coffeepeek.room.DatabaseCore
import com.coffeepeek.room.repository.readSerializable
import kotlin.concurrent.Volatile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class SessionRepositoryImpl(
    database: DatabaseCore,
    private val secureStore: SessionSecureStore,
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
        if (secureStore.read() != null) return
        val legacy = settings.readSerializable<AuthResp>(SESSION_KEY)
            ?: settings.readSerializable<AuthResp>(LEGACY_SESSION_KEY)
            ?: return
        secureStore.write(legacy)
        settings.delete(SESSION_KEY)
        settings.delete(LEGACY_SESSION_KEY)
    }

    override suspend fun getSession(): Session? {
        migrateLegacySessionIfNeeded()
        return secureStore.read()?.toSession()?.also { memoryCache = it }
    }

    override suspend fun persistSession(session: Session?) {
        secureStore.write(session?.toAuthResp())
    }

    override suspend fun saveSession(session: Session?) {
        applySession(session)
        persistSession(session)
    }

    override fun observeSession(): Flow<Session?> = flow {
        migrateLegacySessionIfNeeded()
        emitAll(
            secureStore.observe().map { auth ->
                auth?.toSession().also { memoryCache = it }
            },
        )
    }

    override suspend fun isLoggedIn(): Boolean =
        isActiveSession(peekSession() ?: getSession())

    override fun hasModeratorAccess(): Boolean {
        val session = peekSession() ?: return false
        if (!isActiveSession(session)) return false
        return JwtUtils.hasModeratorAccess(session.accessToken)
    }

    private fun AuthResp.toSession() = Session(
        accessToken = accessToken,
        refreshToken = refreshToken,
        userId = JwtUtils.extractUserId(accessToken),
    )

    private fun Session.toAuthResp() = AuthResp(accessToken, refreshToken.orEmpty())
}
