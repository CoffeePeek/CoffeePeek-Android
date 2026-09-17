package com.coffeepeek.admin.utils

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CheckInValidationTest {

    @Test
    fun publicHeaderRequiresAtLeastThreeCharacters() {
        assertNotNull(validatePublicCheckInHeader(""))
        assertNotNull(validatePublicCheckInHeader("ok"))
        assertNull(validatePublicCheckInHeader("Кофе"))
    }

    @Test
    fun publicDescriptionRequiresAtLeastTenCharacters() {
        assertNotNull(validatePublicCheckInDescription(""))
        assertNotNull(validatePublicCheckInDescription("Коротко"))
        assertNull(validatePublicCheckInDescription("Очень хороший кофе"))
    }
}
