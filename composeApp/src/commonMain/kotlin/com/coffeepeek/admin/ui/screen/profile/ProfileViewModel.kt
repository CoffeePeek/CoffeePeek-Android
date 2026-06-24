package com.coffeepeek.admin.ui.screen.profile

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.theme.ThemeManager
import com.coffeepeek.admin.theme.ThemeMode
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.repository.AuthRepository
import com.coffeepeek.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val email: String = "",
    val displayName: String = "",
    val about: String? = null,
    val avatarUrl: String? = null,
    val initials: String = "",
    val reviewCount: Int = 0,
    val checkInCount: Int = 0,
    val addedShopsCount: Int = 0,
    val isLoading: Boolean = true,
    val isLoggingOut: Boolean = false,
    val error: String? = null,
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = ThemeManager.themeMode

    init {
        loadProfile()
    }

    fun loadProfile() {
        workScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            userRepository.getMe()
                .onSuccess { profile ->
                    _uiState.update { it.copy(
                        email          = profile.email,
                        displayName    = profile.userName,
                        about          = profile.about,
                        avatarUrl      = profile.avatarUrl,
                        initials       = buildInitials(profile.userName),
                        reviewCount    = profile.reviewCount,
                        checkInCount   = profile.checkInCount,
                        addedShopsCount = profile.addedShopsCount,
                        isLoading      = false,
                    ) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = err.message ?: "Ошибка загрузки профиля",
                    ) }
                }
        }
    }

    fun setTheme(mode: ThemeMode) {
        ThemeManager.setTheme(mode)
    }

    fun logout() {
        workScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            authRepository.logout()
            Navigator.navigate(Navigator.Screen.Auth)
        }
    }

    private fun buildInitials(name: String): String {
        val parts = name.trim().split(" ", "_", ".")
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}".uppercase()
            name.length >= 2 -> name.take(2).uppercase()
            name.isNotEmpty() -> name.first().uppercase()
            else -> "?"
        }
    }
}
