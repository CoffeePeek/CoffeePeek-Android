package com.coffeepeek.data.repository

import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.domain.model.Session
import com.coffeepeek.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.DatabaseCore
import org.example.project.repository.readSerializable
import org.example.project.repository.readSerializableFlow
import org.example.project.repository.saveSerializable

class SessionRepositoryImpl(
    database: DatabaseCore,
    private val scope: CoroutineScope,
) : SessionRepository {

    companion object {
        private const val SESSION_KEY = "SESSION"
    }

    private val settings = database.settingRepository

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
