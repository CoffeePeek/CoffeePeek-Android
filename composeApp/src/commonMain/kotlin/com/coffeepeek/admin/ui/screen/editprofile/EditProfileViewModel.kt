package com.coffeepeek.admin.ui.screen.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val username: String = "",
    val about: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
) {
    val usernameError: String? get() = when {
        username.isBlank() -> "Имя не может быть пустым"
        username.length < 2 -> "Минимум 2 символа"
        username.length > 64 -> "Максимум 64 символа"
        else -> null
    }
    val aboutError: String? get() = if (about.length > 600) "Максимум 600 символов" else null
    val canSave get() = usernameError == null && aboutError == null && !isSaving
}

class EditProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            userRepository.getMe()
                .onSuccess { profile ->
                    _state.update { it.copy(
                        username  = profile.userName,
                        about     = profile.about.orEmpty(),
                        isLoading = false,
                    ) }
                }
                .onFailure { err ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = err.message ?: "Ошибка загрузки профиля",
                    ) }
                }
        }
    }

    fun onUsernameChange(v: String) { _state.update { it.copy(username = v.take(64)) } }
    fun onAboutChange(v: String)    { _state.update { it.copy(about = v.take(600)) } }
    fun clearError()                { _state.update { it.copy(error = null) } }

    fun save() {
        val s = _state.value
        if (!s.canSave) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            var usernameOk = true
            var aboutOk = true

            userRepository.updateUsername(s.username.trim())
                .onFailure { err ->
                    usernameOk = false
                    _state.update { it.copy(error = err.message ?: "Ошибка обновления имени") }
                }

            if (usernameOk) {
                userRepository.updateAbout(s.about.trim())
                    .onFailure { err ->
                        aboutOk = false
                        _state.update { it.copy(error = err.message ?: "Ошибка обновления описания") }
                    }
            }

            _state.update { it.copy(isSaving = false, saveSuccess = usernameOk && aboutOk) }

            if (usernameOk && aboutOk) {
                Navigator.popBack()
            }
        }
    }
}
