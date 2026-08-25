package com.coffeepeek.admin.ui.model

data class CoffeeFocusOption(
    val id: String,
    val label: String,
)

val COFFEE_FOCUS_OPTIONS: List<CoffeeFocusOption> = listOf(
    CoffeeFocusOption(id = "specialty", label = "Specialty"),
    CoffeeFocusOption(id = "coffee_bar", label = "Кофейня"),
    CoffeeFocusOption(id = "cafe", label = "Кафе"),
)
