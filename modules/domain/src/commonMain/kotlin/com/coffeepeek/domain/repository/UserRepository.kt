package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.PendingPhotoUpload
import com.coffeepeek.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    fun observeProfile(): StateFlow<UserProfile?>

    suspend fun refreshProfile(): Result<UserProfile>

    suspend fun getMe(): Result<UserProfile>

    suspend fun updateUsername(username: String): Result<Unit>

    suspend fun updateAbout(about: String): Result<Unit>

    suspend fun updateAvatar(photo: PendingPhotoUpload): Result<Unit>
}
