package com.coffeepeek.admin.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class ShareHelperTest {
    @Test
    fun appShareTextMatchesProductCopy() {
        assertEquals(
            "Нашёл отличное приложения, которое собирает кофейни и людей, которые любят кофе — загляни: https://coffeepeek.by",
            COFFEEPEEK_SHARE_TEXT,
        )
    }
}
