package com.coffeepeek.admin.di

import com.coffeepeek.admin.locator.Locator
import com.coffeepeek.feature.favorites.di.favoritesRoomModule
import com.coffeepeek.feature.favorites.di.legacyFavoritesConsumersModule

actual fun initPlatformKoin() {
    initKoin(
        registerLegacyFavorites = false,
        platformModules = listOf(
            favoritesRoomModule(Locator.database.settingRepository),
            legacyFavoritesConsumersModule(),
        ),
    )
}
