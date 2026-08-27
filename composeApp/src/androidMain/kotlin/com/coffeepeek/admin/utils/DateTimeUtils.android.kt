package com.coffeepeek.admin.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal actual fun currentUtcIsoDateTime(): String = Instant.now().toString()

internal actual fun currentEpochMillis(): Long = System.currentTimeMillis()

internal actual fun epochMillisToIsoInstant(millis: Long): String =
    Instant.ofEpochMilli(millis).toString()

private val visitDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))

internal actual fun formatVisitDate(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(visitDateFormatter)
