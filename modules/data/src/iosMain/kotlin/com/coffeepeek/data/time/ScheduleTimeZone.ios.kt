package com.coffeepeek.data.time

import platform.Foundation.NSTimeZone

internal actual fun currentUtcOffsetMinutes(): Int =
    (NSTimeZone.localTimeZone.secondsFromGMT / 60).toInt()
