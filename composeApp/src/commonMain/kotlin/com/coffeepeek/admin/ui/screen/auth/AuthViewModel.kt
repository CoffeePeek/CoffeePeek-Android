package com.coffeepeek.admin.ui.screen.auth

import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.no_login
import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.validateEmailRequired
import com.coffeepeek.admin.utils.validatePasswordRequired
import com.coffeepeek.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val authRepository: AuthRepository,
) : BaseViewModel() {

    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _emailError = MutableStateFlow<String?>(null)
    private val _passwordError = MutableStateFlow<String?>(null)

    val email = _email.asStateFlow()
    val password = _password.asStateFlow()
    val emailError = _emailError.asStateFlow()
    val passwordError = _passwordError.asStateFlow()

    fun onEmailChange(newValue: String) {
        _email.value = newValue
        if (_emailError.value != null) {
            _emailError.value = validateEmailRequired(newValue)
        }
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
        if (_passwordError.value != null) {
            _passwordError.value = validatePasswordRequired(newValue)
        }
    }

    fun onLoginClick() {
        val emailValue = _email.value.trim()
        val passwordValue = _password.value
        val emailErr = validateEmailRequired(emailValue)
        val passwordErr = validatePasswordRequired(passwordValue)
        _emailError.value = emailErr
        _passwordError.value = passwordErr
        if (emailErr != null || passwordErr != null) return

        launchRequest(
            errorMessage = Res.string.no_login,
            onSuccess = { Navigator.navigate(Navigator.Screen.Main) },
        ) {
            authRepository.login(emailValue, passwordValue).getOrThrow()
        }
    }

    fun onGoogleLogin(idToken: String) {
        launchRequest(
            errorMessage = Res.string.no_login,
            onSuccess = { Navigator.navigate(Navigator.Screen.Main) },
        ) {
            authRepository.googleLogin(idToken).getOrThrow()
        }
    }

    fun clearFieldErrors() {
        _emailError.value = null
        _passwordError.value = null
    }
}
