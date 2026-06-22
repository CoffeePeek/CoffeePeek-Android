package com.coffeepeek.admin.ui.screen.auth.registr

import androidx.lifecycle.viewModelScope
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.email_no_exist
import coffeepeek.composeapp.generated.resources.error_email_name
import coffeepeek.composeapp.generated.resources.error_enter_name
import coffeepeek.composeapp.generated.resources.error_enter_password
import coffeepeek.composeapp.generated.resources.error_enter_password_length
import coffeepeek.composeapp.generated.resources.error_registr
import coffeepeek.composeapp.generated.resources.error_term_of_user
import coffeepeek.composeapp.generated.resources.error_valid_email
import coffeepeek.composeapp.generated.resources.maybe_later
import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.ErrorHandler
import com.coffeepeek.domain.repository.AuthRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@OptIn(FlowPreview::class)
class RegisterViewModel(
    private val authRepository: AuthRepository,
) : BaseViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isTermsAccepted = MutableStateFlow(false)
    val isTermsAccepted = _isTermsAccepted.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()

    init {
        observePasswordInput()
        observeEmailInput()
    }

    private fun observeEmailInput() {
        workScope.launch {
            _email
                .filter { it.length > 3 }
                .debounce(700)
                .distinctUntilChanged()
                .collectLatest { emailToCheck ->
                    checkEmailAvailability(emailToCheck)
                }
        }
    }

    fun onNameChange(value: String) {
        _name.value = value
    }

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        if (_passwordError.value != null) {
            _passwordError.value = null
        }
    }

    fun onTermsCheckedChange(checked: Boolean) {
        _isTermsAccepted.value = checked
    }

    private fun observePasswordInput() {
        viewModelScope.launch {
            _password
                .debounce(700)
                .distinctUntilChanged()
                .collectLatest { input ->
                    if (input.isNotEmpty() && input.length < 6) {
                        _passwordError.value = "Пароль должен содержать минимум 6 символов"
                    } else {
                        _passwordError.value = null
                    }
                }
        }
    }

    private suspend fun checkEmailAvailability(email: String) {
        try {
            val isTaken = authRepository.isEmailTaken(email).getOrThrow()
            _emailError.value = if (isTaken) {
                getString(Res.string.email_no_exist)
            } else {
                null
            }
        } catch (e: Exception) {
            ErrorHandler.showError(e.message.toString())
        }
    }

    fun onRegisterClick() {
        launchRequest(
            onSuccess = { Navigator.navigate(Navigator.Screen.Auth) },
            errorMessage = Res.string.error_registr,
        ) {
            val currentName = _name.value.trim()
            val currentEmail = _email.value.trim()
            val currentPassword = _password.value

            var isValid = true
            var error: StringResource = Res.string.maybe_later

            if (!_isTermsAccepted.value) {
                isValid = false
                error = Res.string.error_term_of_user
            }

            if (currentPassword.isEmpty()) {
                isValid = false
                error = Res.string.error_enter_password
            } else if (currentPassword.length < 6) {
                _passwordError.value = getString(Res.string.error_enter_password_length)
                isValid = false
                error = Res.string.error_valid_email
            }

            if (currentEmail.isEmpty()) {
                _emailError.value = getString(Res.string.error_email_name)
                isValid = false
                error = Res.string.error_email_name
            } else if (!currentEmail.matches(emailRegex)) {
                _emailError.value = getString(Res.string.error_valid_email)
                isValid = false
                error = Res.string.error_valid_email
            }

            if (currentName.isEmpty()) {
                isValid = false
                error = Res.string.error_enter_name
            }

            if (!isValid) {
                ErrorHandler.showError(getString(error))
                return@launchRequest
            }

            authRepository.register(currentName, currentEmail, currentPassword).getOrThrow()
        }
    }

    fun onGoogleLogin(idToken: String) {
        launchRequest(
            errorMessage = Res.string.maybe_later,
            onSuccess = { Navigator.navigate(Navigator.Screen.Main) },
        ) {
            authRepository.googleLogin(idToken).getOrThrow()
        }
    }
}
