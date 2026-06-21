package com.coffeepeek.admin.ui.screen.auth

import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.no_login
import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val authRepository: AuthRepository,
) : BaseViewModel() {

    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")

    val email = _email.asStateFlow()
    val password = _password.asStateFlow()

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun onLoginClick() {
        launchRequest(
            errorMessage = Res.string.no_login,
            onSuccess = { Navigator.navigate(Navigator.Screen.Main) },
        ) {
            authRepository.login(_email.value, _password.value).getOrThrow()
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
}
