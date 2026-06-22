package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.PendingPhotoUpload
import com.coffeepeek.domain.model.UserProfile

interface UserRepository {
    suspend fun getMe(): Result<UserProfile>
    suspend fun updateUsername(username: String): Result<Unit>
    suspend fun updateAbout(about: String): Result<Unit>
    suspend fun updateAvatar(photo: PendingPhotoUpload): Result<Unit>
}
