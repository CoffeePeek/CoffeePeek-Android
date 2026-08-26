package com.coffeepeek.admin.ui.model

import com.coffeepeek.domain.model.CoffeeShopType

data class CoffeeFocusOption(
    val id: String,
    val label: String,
)

val COFFEE_FOCUS_OPTIONS: List<CoffeeFocusOption> = listOf(
    CoffeeFocusOption(id = CoffeeShopType.SPECIALTY, label = "Specialty"),
    CoffeeFocusOption(id = CoffeeShopType.COFFEE_BAR, label = "Кофейня"),
    CoffeeFocusOption(id = CoffeeShopType.CAFE, label = "Кафе"),
)
