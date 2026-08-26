package com.coffeepeek.data.session

import com.coffeepeek.domain.repository.FavoriteRepository
import com.coffeepeek.domain.repository.SessionRepository
import java.io.File

class UserSessionCleaner(
    private val sessionRepository: SessionRepository,
    private val favoriteRepository: FavoriteRepository,
    private val httpCacheFolder: File,
    private val appCacheRoot: File,
) {
    suspend fun clearLocalUserData() {
        sessionRepository.saveSession(null)
        favoriteRepository.clearAll()
        clearDiskCaches()
    }

    fun clearDiskCaches() {
        runCatching {
            httpCacheFolder.listFiles()?.forEach { it.deleteRecursively() }
            httpCacheFolder.mkdirs()
        }
        runCatching {
            clearPlatformImageCaches(appCacheRoot)
        }
    }
}
