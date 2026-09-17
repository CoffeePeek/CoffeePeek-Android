package com.coffeepeek.admin.ui.screen.shop

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GuestReviewVisibilityTest {
    @Test
    fun guestCanReadOnlyFirstReview() {
        assertFalse(shouldBlurReview(isLoggedIn = false, reviewIndex = 0))
        assertTrue(shouldBlurReview(isLoggedIn = false, reviewIndex = 1))
        assertTrue(shouldBlurReview(isLoggedIn = false, reviewIndex = 5))
    }

    @Test
    fun loggedInUserCanReadEveryReview() {
        assertFalse(shouldBlurReview(isLoggedIn = true, reviewIndex = 0))
        assertFalse(shouldBlurReview(isLoggedIn = true, reviewIndex = 1))
    }
}
