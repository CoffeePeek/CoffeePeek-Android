@file:OptIn(kotlinx.cinterop.BetaInteropApi::class)

package com.coffeepeek.admin.utils

import platform.Foundation.NSDate
import platform.Foundation.NSISO8601DateFormatter

internal actual fun currentUtcIsoDateTime(): String = isoFormatter().stringFromDate(NSDate())

private fun isoFormatter() = NSISO8601DateFormatter()
