package com.coffeepeek.admin.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class ShareHelperTest {
    @Test
    fun appShareTextMatchesProductCopy() {
        assertEquals(
            "Делюсь отличным приложением, которое объединяет кофейни и любителей кофе — загляни: https://coffeepeek.by",
            COFFEEPEEK_SHARE_TEXT,
        )
    }
}
