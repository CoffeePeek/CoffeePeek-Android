package com.coffeepeek.admin.utils

import kotlin.math.abs
import kotlin.math.round

internal fun formatOneDecimal(value: Number): String {
    val tenths = round(value.toDouble() * 10.0).toLong()
    val absolute = abs(tenths)
    val sign = if (tenths < 0) "-" else ""
    return "$sign${absolute / 10}.${absolute % 10}"
}

internal fun formatMinutesAsClock(minutes: Int): String {
    val clamped = minutes.coerceIn(0, 23 * 60 + 59)
    return "${(clamped / 60).toString().padStart(2, '0')}:" +
        (clamped % 60).toString().padStart(2, '0')
}
