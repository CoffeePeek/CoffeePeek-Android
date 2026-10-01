package com.coffeepeek.data.time

import com.coffeepeek.domain.model.ScheduleInterval
import com.coffeepeek.domain.model.ShopSchedule
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleTimeZoneTest {
    @Test
    fun shiftsHoursAndDayAcrossUtcBoundary() {
        val local = listOf(
            ShopSchedule(
                dayOfWeek = 1,
                isClosed = false,
                intervals = listOf(ScheduleInterval("01:00", "05:00")),
            ),
        )

        val utc = shiftSchedules(local, minutesDelta = -180)
        assertEquals(
            ScheduleInterval("22:00", "02:00"),
            utc.single { it.dayOfWeek == 0 }.intervals.single(),
        )

        val restored = shiftSchedules(utc, minutesDelta = 180)
        assertEquals(local.single(), restored.single { it.dayOfWeek == 1 })
    }
}
