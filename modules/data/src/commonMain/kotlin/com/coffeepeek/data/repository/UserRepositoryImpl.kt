package com.coffeepeek.data.repository

import com.coffeepeek.api.model.request.UploadedPhotoReq
import com.coffeepeek.api.service.UserApiService
import com.coffeepeek.domain.model.PendingPhotoUpload
import com.coffeepeek.domain.model.UserProfile
import com.coffeepeek.domain.repository.PhotoRepository
import com.coffeepeek.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userApiService: UserApiService,
    private val photoRepository: PhotoRepository,
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

    override suspend fun updateAvatar(photo: PendingPhotoUpload): Result<Unit> = runCatching {
        val meta = photoRepository.uploadAvatar(photo)
            .getOrElse { error("Не удалось загрузить фото: ${it.message ?: "неизвестная ошибка"}") }
        userApiService.updateAvatar(
            UploadedPhotoReq(
                fileName = meta.fileName,
                contentType = meta.contentType,
                storageKey = meta.storageKey,
                size = meta.size,
            ),
        ).getOrElse { error("Не удалось сохранить аватар: ${it.message ?: "неизвестная ошибка"}") }
    }
}
