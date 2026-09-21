package com.coffeepeek.admin.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PasswordValidationTest {

    @Test
    fun requiredAuthFieldsUseConciseRussianMessages() {
        assertEquals("Введите email", validateEmailRequired(""))
        assertEquals("Введите корректный email", validateEmailRequired("broken-address"))
        assertEquals("Введите пароль", validatePasswordRequired(""))
    }

    @Test
    fun registrationPasswordRequiresAtLeastEightCharacters() {
        assertEquals(
            "Пароль должен содержать как минимум 8 символов",
            validatePasswordRequired(
                password = "1234567",
                minLength = MIN_REGISTRATION_PASSWORD_LENGTH,
            ),
        )
        assertNull(
            validatePasswordRequired(
                password = "12345678",
                minLength = MIN_REGISTRATION_PASSWORD_LENGTH,
            ),
        )
    }

    @Test
    fun loginApiErrorsAreLocalized() {
        assertEquals(
            "Неверный email или пароль",
            localizedLoginError("Invalid credentials"),
        )
        assertEquals(
            "Не удалось подключиться. Проверьте интернет-соединение",
            localizedLoginError("Connection timeout"),
        )
        assertEquals(
            "Не удалось войти. Проверьте email и пароль",
            localizedLoginError("Unexpected server response"),
        )
    }
}
