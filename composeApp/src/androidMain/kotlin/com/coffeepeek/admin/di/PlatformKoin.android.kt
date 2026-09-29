package com.coffeepeek.admin.di

import com.coffeepeek.admin.di.favorites.favoritesRoomModule
import com.coffeepeek.admin.di.favorites.legacyFavoritesConsumersModule
import com.coffeepeek.admin.locator.Locator

actual fun initPlatformKoin() {
    initKoin(
        registerLegacyFavorites = false,
        platformModules = listOf(
            favoritesRoomModule(Locator.database.settingRepository),
            legacyFavoritesConsumersModule(),
        ),
    )
}
