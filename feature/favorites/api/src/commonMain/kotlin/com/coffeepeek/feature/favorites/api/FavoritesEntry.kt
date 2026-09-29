package com.coffeepeek.feature.favorites.api

import androidx.compose.runtime.Composable

/** Minimal screen construction contract; native UI hosts can use domain instead. */
interface FavoritesEntry {
    @Composable
    fun Content(
        onOpenShop: (String) -> Unit,
        onBack: () -> Unit,
        distanceForShop: (String) -> String? = { null },
    )
}
