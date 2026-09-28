package com.coffeepeek.admin.ui.favorites

import androidx.compose.runtime.Composable
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.feature.favorites.api.FavoritesEntry
import org.koin.compose.koinInject

@Composable
internal actual fun FavoritesDestination() {
    FavoritesDestination(koinInject<FavoritesEntry>())
}

@Composable
internal fun FavoritesDestination(entry: FavoritesEntry) {
    entry.Content(
        onOpenShop = { shopId -> Navigator.navigate(Navigator.Screen.ShopDetail(shopId)) },
        onBack = Navigator::popBack,
    )
}
