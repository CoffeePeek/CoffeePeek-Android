package com.coffeepeek.admin.di

import com.coffeepeek.admin.locator.Constants
import com.coffeepeek.admin.locator.Locator
import com.coffeepeek.admin.ui.NavigatorViewModel
import com.coffeepeek.admin.ui.screen.auth.AuthViewModel
import com.coffeepeek.admin.ui.screen.auth.registr.RegisterViewModel
import com.coffeepeek.admin.ui.screen.feed.FeedViewModel
import com.coffeepeek.admin.ui.screen.addshop.AddShopViewModel
import com.coffeepeek.admin.ui.screen.editprofile.EditProfileViewModel
import com.coffeepeek.admin.ui.screen.map.MapViewModel
import com.coffeepeek.admin.ui.screen.profile.ProfileViewModel
import com.coffeepeek.admin.ui.screen.shop.ShopDetailViewModel
import com.coffeepeek.data.di.dataModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoin() {
    startKoin {
        modules(
            dataModule(
                baseUrl = Constants.BASE_URL,
                cacheFolder = Locator.cacheFolder,
                database = Locator.database,
                debug = true,
            ),
            appModule(),
        )
    }
}

private fun appModule() = module {
    factory { AuthViewModel(get()) }
    factory { RegisterViewModel(get()) }
    factory { NavigatorViewModel(get()) }
    factory { FeedViewModel(get()) }
    factory { MapViewModel(get()) }
    factory { (shopId: String) -> ShopDetailViewModel(shopId, get()) }
    factory { ProfileViewModel(get(), get(), get()) }
    factory { AddShopViewModel(get()) }
    factory { EditProfileViewModel(get()) }
}
