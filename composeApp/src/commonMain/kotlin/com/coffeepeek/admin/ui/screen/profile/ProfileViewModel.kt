package com.coffeepeek.admin.ui.screen.profile

import com.coffeepeek.admin.theme.ThemeManager
import com.coffeepeek.admin.theme.ThemeMode
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.model.UserProfile
import com.coffeepeek.domain.repository.AuthRepository
import com.coffeepeek.domain.repository.SessionRepository
import com.coffeepeek.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val refreshError: String? = null,
) {
    val hasContent: Boolean
        get() = displayName.isNotBlank() || !avatarUrl.isNullOrBlank()
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
) {
    private val workScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = ThemeManager.themeMode

    private var loadedForUserId: String? = null

    init {
        observeProfileCache()
        observeSessionChanges()
    }

    fun refreshProfile() {
        workScope.launch {
            val current = _uiState.value
            val showFullScreenLoader = !current.hasContent && current.error == null
            _uiState.update {
                it.copy(
                    isLoading = showFullScreenLoader,
                    isRefreshing = !showFullScreenLoader,
                    error = if (showFullScreenLoader) null else it.error,
                    refreshError = null,
                )
            }
            userRepository.refreshProfile()
                .onFailure { err ->
                    val message = err.message ?: "Ошибка загрузки профиля"
                    _uiState.update { state ->
                        if (state.hasContent) {
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                refreshError = message,
                            )
                        } else {
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = message,
                            )
                        }
                    }
                }
        }
    }

    fun setTheme(mode: ThemeMode) {
        ThemeManager.setTheme(mode)
    }

    fun logout() {
        workScope.launch {
            authRepository.logout()
            resetProfileState()
            Navigator.navigate(Navigator.Screen.Auth)
        }
    }

    private fun observeProfileCache() {
        workScope.launch {
            userRepository.observeProfile().collect { profile ->
                if (profile != null) {
                    applyProfile(profile)
                }
            }
        }
    }

    private fun observeSessionChanges() {
        workScope.launch {
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
                        resetProfileState()
                    } else if (userId != loadedForUserId) {
                        if (loadedForUserId != null) {
                            resetProfileState()
                            _uiState.value = ProfileUiState(isLoading = true)
                        }
                        loadedForUserId = userId
                        if (userRepository.observeProfile().value == null) {
                            refreshProfile()
                        }
                    }
                }
        }
    }

    private fun applyProfile(profile: UserProfile) {
        _uiState.update {
            it.copy(
                email = profile.email,
                displayName = profile.userName,
                about = profile.about,
                avatarUrl = profile.avatarUrl,
                initials = buildInitials(profile.userName),
                reviewCount = profile.reviewCount,
                checkInCount = profile.checkInCount,
                addedShopsCount = profile.addedShopsCount,
                isLoading = false,
                isRefreshing = false,
                error = null,
                refreshError = null,
            )
        }
    }

    private fun resetProfileState() {
        loadedForUserId = null
        _uiState.value = ProfileUiState(isLoading = false)
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
