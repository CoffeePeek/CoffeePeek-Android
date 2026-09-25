package com.coffeepeek.admin.ui.screen.checkins

import kotlin.test.Test
import kotlin.test.assertEquals

class CheckInCalendarTest {
    @Test
    fun september2026StartsOnTuesdayAndHasThirtyDays() {
        val cells = calendarCells(CalendarMonth(2026, 9))

        assertEquals(35, cells.size)
        assertEquals(listOf(null, 1, 2, 3, 4, 5, 6), cells.take(7))
        assertEquals(30, cells.filterNotNull().last())
    }

    @Test
    fun rangeEndsAtNextMonthAndLeapDayIsPresent() {
        val february = CalendarMonth(2024, 2)

        assertEquals("2024-02-01T00:00:00Z", february.fromUtc)
        assertEquals("2024-03-01T00:00:00Z", february.toUtc)
        assertEquals(29, calendarCells(february).filterNotNull().last())
    }

    @Test
    fun consecutiveCheckInsFormAStreak() {
        val month = CalendarMonth(2026, 9)
        val dates = setOf("2026-09-05", "2026-09-06", "2026-09-07")

        assertEquals(CheckInStreak(false, true), checkInStreak(month, 5, dates))
        assertEquals(CheckInStreak(true, true), checkInStreak(month, 6, dates))
        assertEquals(CheckInStreak(true, false), checkInStreak(month, 7, dates))
        assertEquals(CheckInStreak(false, false), checkInStreak(month, 9, dates))
    }

    @Test
    fun daysWithoutCheckInsNeverJoinAStreak() {
        val month = CalendarMonth(2026, 9)
        val dates = setOf("2026-09-05", "2026-09-06", "2026-09-08")

        assertEquals(CheckInStreak(false, false), checkInStreak(month, 4, dates))
        assertEquals(CheckInStreak(false, false), checkInStreak(month, 7, dates))
        assertEquals(CheckInStreak(false, false), checkInStreak(month, 8, dates))
    }
}
