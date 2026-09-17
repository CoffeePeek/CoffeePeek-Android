package com.coffeepeek.admin.ui.screen.review

import kotlin.test.Test
import kotlin.test.assertEquals

class ReviewRatingDefaultsTest {

    @Test
    fun newReviewUsesVisibleFourStarDefaults() {
        val state = CreateReviewUiState()

        assertEquals(4, state.coffeeRating)
        assertEquals(4, state.serviceRating)
        assertEquals(4, state.placeRating)
    }
}
