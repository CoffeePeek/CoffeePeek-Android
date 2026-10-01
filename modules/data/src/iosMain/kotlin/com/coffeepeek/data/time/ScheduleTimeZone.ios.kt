package com.coffeepeek.data.time

import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone
import platform.Foundation.secondsFromGMT

internal actual fun currentUtcOffsetMinutes(): Int =
    (NSTimeZone.localTimeZone().secondsFromGMT() / 60).toInt()
