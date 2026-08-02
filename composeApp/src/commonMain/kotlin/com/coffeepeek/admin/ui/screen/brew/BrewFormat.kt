package com.coffeepeek.admin.ui.screen.brew

import com.coffeepeek.domain.model.BrewMethod
import com.coffeepeek.domain.model.RoastLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatDuration(totalSec: Int): String {
    val safe = totalSec.coerceAtLeast(0)
    val m = safe / 60
    val s = safe % 60
    return "%d:%02d".format(m, s)
}

internal fun formatBrewDate(epochMs: Long): String {
    val fmt = SimpleDateFormat("d MMM, HH:mm", Locale("ru"))
    return fmt.format(Date(epochMs))
}

internal fun BrewMethod.yieldLabel(): String = when (this) {
    BrewMethod.ESPRESSO -> "Выход, г"
    else -> "Вода, г"
}

internal fun defaultDose(method: BrewMethod): String = when (method) {
    BrewMethod.ESPRESSO -> "18"
    BrewMethod.FILTER -> "15"
    BrewMethod.MOKA -> "16"
    BrewMethod.CEZVE -> "10"
    BrewMethod.OTHER -> "15"
}

internal fun defaultYield(method: BrewMethod): String = when (method) {
    BrewMethod.ESPRESSO -> "36"
    BrewMethod.FILTER -> "250"
    BrewMethod.MOKA -> "80"
    BrewMethod.CEZVE -> "100"
    BrewMethod.OTHER -> "200"
}

internal fun RoastLevel.label(): String = labelRu
