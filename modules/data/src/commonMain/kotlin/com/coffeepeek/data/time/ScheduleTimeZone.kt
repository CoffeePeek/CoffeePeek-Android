package com.coffeepeek.data.time

import com.coffeepeek.domain.model.ScheduleInterval
import com.coffeepeek.domain.model.ShopSchedule

internal expect fun currentUtcOffsetMinutes(): Int

// ponytail: A weekly schedule has no date, so use the device's current UTC offset.
// The API must add an IANA time-zone ID if schedules must stay DST-stable year-round.
internal fun localSchedulesToUtc(schedules: List<ShopSchedule>): List<ShopSchedule> =
    shiftSchedules(schedules, -currentUtcOffsetMinutes())

internal fun utcSchedulesToLocal(schedules: List<ShopSchedule>): List<ShopSchedule> =
    shiftSchedules(schedules, currentUtcOffsetMinutes())

internal fun shiftSchedules(
    schedules: List<ShopSchedule>,
    minutesDelta: Int,
): List<ShopSchedule> {
    if (schedules.isEmpty()) return emptyList()
    val intervalsByDay = mutableMapOf<Int, MutableList<ScheduleInterval>>()
    schedules.filterNot { it.isClosed }.forEach { schedule ->
        schedule.intervals.forEach { interval ->
            val open = shiftTime(interval.openTime, minutesDelta)
            val close = shiftTime(interval.closeTime, minutesDelta)
            val day = normalizeDay(schedule.dayOfWeek + open.dayDelta)
            intervalsByDay.getOrPut(day, ::mutableListOf) += ScheduleInterval(open.value, close.value)
        }
    }
    return (0..6).map { day ->
        val intervals = intervalsByDay[day].orEmpty()
        ShopSchedule(dayOfWeek = day, isClosed = intervals.isEmpty(), intervals = intervals)
    }
}

private data class ShiftedTime(val value: String, val dayDelta: Int)

private fun shiftTime(value: String, minutesDelta: Int): ShiftedTime {
    val parts = value.split(':')
    val hours = parts.getOrNull(0)?.toIntOrNull()
    val minutes = parts.getOrNull(1)?.toIntOrNull()
    if (hours !in 0..23 || minutes !in 0..59) return ShiftedTime(value, 0)

    val shifted = hours!! * 60 + minutes!! + minutesDelta
    val dayDelta = if (shifted < 0) (shifted - 1_439) / 1_440 else shifted / 1_440
    val normalized = ((shifted % 1_440) + 1_440) % 1_440
    return ShiftedTime(
        value = "${(normalized / 60).toString().padStart(2, '0')}:${(normalized % 60).toString().padStart(2, '0')}",
        dayDelta = dayDelta,
    )
}

private fun normalizeDay(day: Int): Int = ((day % 7) + 7) % 7
