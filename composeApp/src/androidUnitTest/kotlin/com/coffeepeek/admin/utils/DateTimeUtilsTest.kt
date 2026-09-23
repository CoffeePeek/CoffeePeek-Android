package com.coffeepeek.admin.utils

import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeUtilsTest {
    @Test
    fun localCalendarDateIsSentAsUtcAndBackendUtcIsShownLocally() {
        val kyiv = ZoneId.of("Europe/Kyiv")

        assertEquals(
            "2026-06-14T21:00:00Z",
            datePickerMillisToUtcIsoInstant(
                Instant.parse("2026-06-15T00:00:00Z").toEpochMilli(),
                kyiv,
            ),
        )
        assertEquals("2026-06-16", utcIsoToLocalDate("2026-06-15T22:30:00Z", kyiv))
        assertEquals(
            "16.06.2026 01:30",
            utcIsoToLocalDateTime("2026-06-15T22:30:00Z", kyiv),
        )
    }
}
