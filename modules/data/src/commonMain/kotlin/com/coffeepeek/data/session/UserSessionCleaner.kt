package com.coffeepeek.data.session

import com.coffeepeek.domain.repository.FavoriteRepository
import com.coffeepeek.domain.repository.SessionRepository

class UserSessionCleaner(
    private val sessionRepository: SessionRepository,
    private val favoriteRepository: FavoriteRepository,
    private val httpCacheFolderPath: String,
    private val appCacheRootPath: String,
) {
    suspend fun clearLocalUserData() {
        sessionRepository.saveSession(null)
        favoriteRepository.clearAll()
        clearDiskCaches()
    }

    fun clearDiskCaches() {
        runCatching { clearPlatformCaches(httpCacheFolderPath, appCacheRootPath) }
    }
}
