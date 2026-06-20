package com.coffeepeek.data.repository

import com.coffeepeek.api.service.UserApiService
import com.coffeepeek.domain.model.UserProfile
import com.coffeepeek.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userApiService: UserApiService,
) : UserRepository {

    override suspend fun getMe(): Result<UserProfile> =
        userApiService.getMe().map { dto ->
            UserProfile(
                userName       = dto.userName,
                email          = dto.email,
                about          = dto.about,
                avatarUrl      = dto.avatarUrl,
                reviewCount    = dto.reviewCount,
                checkInCount   = dto.checkInCount,
                addedShopsCount = dto.addedShopsCount,
            )
        }

    override suspend fun updateUsername(username: String): Result<Unit> =
        userApiService.updateUsername(username)

    override suspend fun updateAbout(about: String): Result<Unit> =
        userApiService.updateAbout(about)
}
