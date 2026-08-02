package com.coffeepeek.admin.ui.screen.editprofile

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.PickedImage
import com.coffeepeek.domain.model.PendingPhotoUpload
import com.coffeepeek.domain.model.UserProfile
import com.coffeepeek.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val username: String = "",
    val about: String = "",
    val avatarUrl: String? = null,
    val pendingAvatar: PickedImage? = null,
    val originalUsername: String = "",
    val originalAbout: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
) {
    val usernameError: String? get() = when {
        username.isBlank() -> "Имя не может быть пустым"
        username.length < 2 -> "Минимум 2 символа"
        username.length > 64 -> "Максимум 64 символа"
        else -> null
    }
    val aboutError: String? get() = if (about.length > 600) "Максимум 600 символов" else null
    val hasChanges: Boolean get() =
        username.trim() != originalUsername ||
            about.trim() != originalAbout ||
            pendingAvatar != null
    val canSave get() = usernameError == null && aboutError == null && hasChanges && !isSaving
}

class EditProfileViewModel(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        workScope.launch {
            val cached = userRepository.observeProfile().value
            if (cached != null) {
                applyProfile(cached, isLoading = false)
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }
            userRepository.refreshProfile()
                .onSuccess { profile -> applyProfile(profile, isLoading = false) }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = err.message ?: "Ошибка загрузки профиля",
                        )
                    }
                }
        }
    }

    fun onUsernameChange(v: String) { _state.update { it.copy(username = v.take(64)) } }
    fun onAboutChange(v: String)    { _state.update { it.copy(about = v.take(600)) } }
    fun onAvatarPicked(image: PickedImage) {
        if (image.bytes.size > 5 * 1024 * 1024) {
            _state.update { it.copy(error = "Размер файла не должен превышать 5 МБ") }
            return
        }
        _state.update { it.copy(pendingAvatar = image, error = null) }
    }
    fun clearError() { _state.update { it.copy(error = null) } }

    fun save() {
        val s = _state.value
        if (!s.canSave) return

        workScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            var success = true

            if (s.pendingAvatar != null) {
                val photo = PendingPhotoUpload(
                    fileName = s.pendingAvatar.fileName,
                    contentType = s.pendingAvatar.contentType,
                    bytes = s.pendingAvatar.bytes,
                )
                userRepository.updateAvatar(photo)
                    .onFailure { err ->
                        success = false
                        _state.update {
                            it.copy(
                                error = err.message
                                    ?: err.cause?.message
                                    ?: "Ошибка обновления аватара",
                            )
                        }
                    }
            }

            if (success && s.username.trim() != s.originalUsername) {
                userRepository.updateUsername(s.username.trim())
                    .onFailure { err ->
                        success = false
                        _state.update { it.copy(error = err.message ?: "Ошибка обновления имени") }
                    }
            }

            if (success && s.about.trim() != s.originalAbout) {
                userRepository.updateAbout(s.about.trim())
                    .onFailure { err ->
                        success = false
                        _state.update { it.copy(error = err.message ?: "Ошибка обновления описания") }
                    }
            }

            _state.update { it.copy(isSaving = false) }

            if (success) {
                Navigator.popBack()
            }
        }
    }

    private fun applyProfile(profile: UserProfile, isLoading: Boolean) {
        _state.update {
            it.copy(
                username = profile.userName,
                about = profile.about.orEmpty(),
                avatarUrl = profile.avatarUrl,
                originalUsername = profile.userName,
                originalAbout = profile.about.orEmpty(),
                isLoading = isLoading,
            )
        }
    }
}
