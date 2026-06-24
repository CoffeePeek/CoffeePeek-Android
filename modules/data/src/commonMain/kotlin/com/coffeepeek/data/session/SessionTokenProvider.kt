package com.coffeepeek.data.session

import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.domain.model.Session
import com.coffeepeek.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SessionTokenProvider(
    private val sessionRepository: SessionRepository,
    scope: CoroutineScope,
) {
    @Volatile
    private var tokens: AuthResp? = null

    init {
        scope.launch {
            tokens = sessionRepository.getSession()?.toAuthResp()
        }
        sessionRepository.observeSession()
            .onEach { session -> tokens = session?.toAuthResp() }
            .launchIn(scope)
    }

    fun current(): AuthResp? = tokens

    private fun Session.toAuthResp() = AuthResp(
        accessToken = accessToken,
        refreshToken = refreshToken.orEmpty(),
    )
}
