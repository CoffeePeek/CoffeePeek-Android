package com.coffeepeek.data.util

import com.coffeepeek.domain.model.Session

object SessionAuth {
    fun isActive(session: Session?): Boolean {
        if (session == null) return false
        if (session.accessToken.isBlank() || session.userId.isNullOrBlank()) return false
        if (!JwtUtils.isAccessTokenExpired(session.accessToken)) return true
        return !session.refreshToken.isNullOrBlank()
    }
}
