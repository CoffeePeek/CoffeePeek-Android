package com.coffeepeek.admin.utils

internal expect fun currentUtcIsoDateTime(): String

/** Current wall-clock time in epoch milliseconds. */
internal expect fun currentEpochMillis(): Long

/** Convert epoch milliseconds to an ISO-8601 UTC instant string (e.g. for `visitedAt`). */
internal expect fun epochMillisToIsoInstant(millis: Long): String

/** Human-readable visit date for the UI, e.g. "27 августа 2026". */
internal expect fun formatVisitDate(millis: Long): String
