package com.coffeepeek.admin.ui.component

import androidx.compose.ui.graphics.vector.ImageVector
import com.coffeepeek.admin.ui.icons.CpIcons

fun shopTagIcon(slug: String?): ImageVector = when (slug?.trim()?.lowercase()) {
    "roastery" -> CpIcons.Factory
    "beans-for-sale" -> CpIcons.ShoppingBag
    "decaf" -> CpIcons.CoffeeBean
    "to_go" -> CpIcons.Footprints
    "plan-based-milk", "plant-based-milk" -> CpIcons.Leaf
    "laptop_friendly" -> CpIcons.Laptop
    "outside-seating" -> CpIcons.Sun
    "breakfasts" -> CpIcons.Egg
    "bakery" -> CpIcons.Bread
    "confectionery" -> CpIcons.Cake
    "pet_friendly" -> CpIcons.PawPrint
    "vegan" -> CpIcons.Avocado
    "gluten-free" -> CpIcons.GrainsSlash
    else -> CpIcons.ListStar
}
