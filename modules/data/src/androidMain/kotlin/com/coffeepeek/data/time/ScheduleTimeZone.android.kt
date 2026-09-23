package com.coffeepeek.data.time

import java.time.Instant
import java.time.ZoneId

internal actual fun currentUtcOffsetMinutes(): Int =
    ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds / 60
