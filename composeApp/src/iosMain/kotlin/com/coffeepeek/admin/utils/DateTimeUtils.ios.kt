@file:OptIn(kotlinx.cinterop.BetaInteropApi::class)

package com.coffeepeek.admin.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSISO8601DateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone

internal actual fun currentUtcIsoDateTime(): String = isoFormatter().stringFromDate(NSDate())

internal actual fun datePickerMillisToUtcIsoInstant(millis: Long): String {
    val selectedUtcDate = NSDate(timeIntervalSince1970 = millis / 1_000.0)
    val date = dateFormatter("yyyy-MM-dd", utc = true).stringFromDate(selectedUtcDate)
    val localMidnight = dateFormatter("yyyy-MM-dd").dateFromString(date) ?: selectedUtcDate
    return isoFormatter().stringFromDate(localMidnight)
}

internal actual fun utcIsoToLocalDate(value: String): String =
    parseUtcDate(value)?.let { dateFormatter("yyyy-MM-dd").stringFromDate(it) }
        ?: value.substringBefore('T').ifBlank { value }

internal actual fun utcIsoToLocalDateTime(value: String): String =
    parseUtcDate(value)?.let { dateFormatter("dd.MM.yyyy HH:mm").stringFromDate(it) } ?: value

private fun isoFormatter() = NSISO8601DateFormatter()

private fun parseUtcDate(value: String): NSDate? {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return null
    isoFormatter().dateFromString(trimmed)?.let { return it }
    return isoFormatter().dateFromString("${trimmed}Z")
}

private fun dateFormatter(pattern: String, utc: Boolean = false) = NSDateFormatter().apply {
    locale = NSLocale(localeIdentifier = "en_US_POSIX")
    dateFormat = pattern
    timeZone = if (utc) NSTimeZone.timeZoneForSecondsFromGMT(0) else NSTimeZone.localTimeZone
}
