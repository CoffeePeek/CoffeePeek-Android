package com.coffeepeek.admin.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PasswordValidationTest {

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
}
