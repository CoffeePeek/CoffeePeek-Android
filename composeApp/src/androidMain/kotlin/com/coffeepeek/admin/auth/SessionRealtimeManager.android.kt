package com.coffeepeek.admin.auth

import androidx.annotation.Keep
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.ErrorHandler
import com.coffeepeek.domain.model.Session
import com.coffeepeek.domain.repository.SessionRepository
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val DEFAULT_REALTIME_SESSION_URL = "https://api.coffeepeek.by/realtime/session"
private const val FORCE_LOGOUT_METHOD = "ForceLogout"

class SessionRealtimeManager(
    private val sessionRepository: SessionRepository,
    private val baseUrl: String,
) : AutoCloseable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionMutex = Mutex()

    @Volatile
    private var hubConnection: HubConnection? = null

    @Volatile
    private var activeAccessToken: String? = null

    @Volatile
    private var started = false

    fun start() {
        if (started) return
        started = true
        sessionRepository.observeSession()
            .map(::activeAccessTokenOrNull)
            .distinctUntilChanged()
            .onEach { token ->
                if (token.isNullOrBlank()) {
                    disconnect()
                } else {
                    ensureConnected(token)
                }
            }
            .launchIn(scope)
    }

    private fun activeAccessTokenOrNull(session: Session?): String? {
        if (!sessionRepository.isActiveSession(session)) return null
        return session?.accessToken?.takeIf { it.isNotBlank() }
    }

    private suspend fun ensureConnected(accessToken: String) {
        if (activeAccessTokenOrNull(sessionRepository.peekSession()) != accessToken) return

        connectionMutex.withLock {
            if (activeAccessTokenOrNull(sessionRepository.peekSession()) != accessToken) return
            if (hubConnection != null && activeAccessToken == accessToken) return

            closeConnectionLocked()
            val connection = createConnection()
            hubConnection = connection
            activeAccessToken = accessToken

            val startResult = runCatching { connection.start().blockingAwait() }
            if (startResult.isFailure) {
                closeConnectionLocked()
                activeAccessToken = null
                scheduleReconnect(accessToken)
            }
        }
    }

    private fun scheduleReconnect(accessToken: String) {
        scope.launch {
            delay(5_000)
            if (activeAccessTokenOrNull(sessionRepository.peekSession()) == accessToken) {
                ensureConnected(accessToken)
            }
        }
    }

    private fun createConnection(): HubConnection {
        // Java SignalR client has no withAutomaticReconnect(); reconnect is handled manually.
        val connection = HubConnectionBuilder
            .create(resolveRealtimeSessionUrl(baseUrl))
            .withAccessTokenProvider(
                Single.defer {
                    val token = activeAccessToken ?: activeAccessTokenOrNull(sessionRepository.peekSession())
                    if (token.isNullOrBlank()) {
                        Single.error(IllegalStateException("No active access token for realtime session"))
                    } else {
                        Single.just(token)
                    }
                },
            )
            .build()

        connection.on(
            FORCE_LOGOUT_METHOD,
            { payload: ForceLogoutPayload ->
                scope.launch { handleForceLogout(payload) }
            },
            ForceLogoutPayload::class.java,
        )

        connection.onClosed {
            val token = activeAccessToken
            if (!token.isNullOrBlank() &&
                activeAccessTokenOrNull(sessionRepository.peekSession()) == token
            ) {
                hubConnection = null
                activeAccessToken = null
                scheduleReconnect(token)
            }
        }

        return connection
    }

    private suspend fun handleForceLogout(payload: ForceLogoutPayload?) {
        disconnect()
        sessionRepository.saveSession(null)
        ErrorHandler.showError(forceLogoutMessage(payload?.reason))
        Navigator.navigate(Navigator.Screen.Auth)
    }

    private fun forceLogoutMessage(reason: String?): String = when (reason?.trim()) {
        "session_revoked" -> "Текущая сессия отозвана. Войдите снова."
        "all_sessions_revoked" -> "Все сессии отозваны. Войдите снова."
        "user_blocked" -> "Аккаунт заблокирован."
        "user_deleted" -> "Аккаунт удалён."
        "password_changed" -> "Пароль изменён. Войдите снова."
        "password_reset" -> "Пароль сброшен. Войдите снова."
        else -> "Сессия завершена на сервере. Войдите снова."
    }

    private suspend fun disconnect() {
        connectionMutex.withLock {
            closeConnectionLocked()
            activeAccessToken = null
        }
    }

    private fun closeConnectionLocked() {
        hubConnection?.let { connection ->
            runCatching { connection.stop().blockingAwait() }
        }
        hubConnection = null
    }

    override fun close() {
        started = false
        runBlocking { disconnect() }
        scope.cancel()
    }
}

private fun resolveRealtimeSessionUrl(baseUrl: String): String {
    val normalized = baseUrl.trim().trimEnd('/')
    if (normalized.isBlank()) return DEFAULT_REALTIME_SESSION_URL
    return "$normalized/realtime/session"
}

@Keep
class ForceLogoutPayload {
    var reason: String? = null
    var occurredAtUtc: String? = null
}
