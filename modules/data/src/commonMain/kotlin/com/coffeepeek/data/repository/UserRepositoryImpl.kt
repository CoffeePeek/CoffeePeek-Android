package com.coffeepeek.data.repository

import com.coffeepeek.api.model.request.UploadedPhotoReq
import com.coffeepeek.api.model.response.UserProfileDto
import com.coffeepeek.api.service.UserApiService
import com.coffeepeek.domain.model.PendingPhotoUpload
import com.coffeepeek.domain.model.UserProfile
import com.coffeepeek.domain.repository.PhotoRepository
import com.coffeepeek.domain.repository.SessionRepository
import com.coffeepeek.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserRepositoryImpl(
    private val userApiService: UserApiService,
    private val photoRepository: PhotoRepository,
    private val sessionRepository: SessionRepository,
    private val scope: CoroutineScope,
) : UserRepository {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    override fun observeProfile(): StateFlow<UserProfile?> = _profile.asStateFlow()

    private var cachedForUserId: String? = null

    init {
        scope.launch {
            sessionRepository.observeSession()
                .map { session ->
                    when {
                        !sessionRepository.isActiveSession(session) -> null
                        else -> session?.userId
                    }
                }
                .distinctUntilChanged()
                .collect { userId ->
                    if (userId == null) {
                        clearProfile()
                    } else if (userId != cachedForUserId) {
                        clearProfile()
                        cachedForUserId = userId
                    }
                }
        }
    }

    override suspend fun refreshProfile(): Result<UserProfile> =
        fetchAndCacheProfile()

    override suspend fun getMe(): Result<UserProfile> {
        _profile.value?.let { return Result.success(it) }
        return refreshProfile()
    }

    override suspend fun updateUsername(username: String): Result<Unit> =
        userApiService.updateUsername(username).onSuccess {
            _profile.update { profile -> profile?.copy(userName = username) }
        }

    override suspend fun updateAbout(about: String): Result<Unit> =
        userApiService.updateAbout(about).onSuccess {
            _profile.update { profile -> profile?.copy(about = about) }
        }

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
        fetchAndCacheProfile().getOrThrow()
        Unit
    }

    private suspend fun fetchAndCacheProfile(): Result<UserProfile> =
        userApiService.getMe().map { dto ->
            dto.toUserProfile().also { profile ->
                _profile.value = profile
                cachedForUserId = sessionRepository.peekSession()?.userId
            }
        }

    private fun clearProfile() {
        cachedForUserId = null
        _profile.value = null
    }
}

private fun UserProfileDto.toUserProfile() = UserProfile(
    userName = userName,
    email = email,
    about = about,
    avatarUrl = avatarUrl,
    reviewCount = reviewCount,
    checkInCount = checkInCount,
    addedShopsCount = addedShopsCount,
)
