package com.coffeepeek.admin.settings

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

class ReviewDraftPolicyTest {

    private fun draft(header: String = "", comment: String = "", rating: Int = 4, savedAt: Long = 0) =
        ReviewDraft(header, comment, rating, rating, rating, savedAt)

    @Test
    fun untouchedFormIsBlankAndNotStored() {
        assertTrue(draft().isBlank(defaultRating = 4))
        assertTrue(draft(header = "   ").isBlank(defaultRating = 4))
    }

    @Test
    fun anyInputMakesDraftWorthKeeping() {
        assertFalse(draft(header = "Вкусно").isBlank(defaultRating = 4))
        assertFalse(draft(comment = "…").isBlank(defaultRating = 4))
        assertFalse(draft(rating = 5).isBlank(defaultRating = 4))
    }

    @Test
    fun draftExpiresOnlyAfterTtl() {
        val savedAt = 1_000_000L
        assertFalse(draft(savedAt = savedAt).isExpired(savedAt + 29.days.inWholeMilliseconds))
        assertTrue(draft(savedAt = savedAt).isExpired(savedAt + 31.days.inWholeMilliseconds))
    }
}
