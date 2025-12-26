package com.coffeepeek.admin.ui.screen.auth

import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.email_no_exist
import coffeepeek.composeapp.generated.resources.no_login
import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.locator.Locator
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.ErrorHandler
import com.coffeepeek.admin.utils.LoadingHandler
import com.coffeepeek.api.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class AuthViewModel : BaseViewModel() {

    private val authService = Locator.repo.authService

    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")

    val email = _email.asStateFlow()
    val password = _password.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    fun onEmailChange(newValue: String) {
        _email.value = newValue
        if (_emailError.value != null) {
            _emailError.value = null
        }
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    private suspend fun checkEmailAvailability(email: String) {
        try {
            val isTaken = authService.checkEmail(email).getOrThrow()

            if (isTaken) {
                _emailError.value = getString(Res.string.email_no_exist)
            } else {
                _emailError.value = null
            }
        } catch (e: Exception) {
            ErrorHandler.showError(e.message.toString())
        }
    }

    fun onLoginClick() {
        launchRequest(
            errorMessage = Res.string.no_login,
            onSuccess = { result ->
                Navigator.navigate(Navigator.Screen.Main)
            }
        ) {
            val id = authService.authLogin(_email.value, _password.value).getOrThrow()
            Locator.setting.setAuth(id)
        }
    }
}