package com.coffeepeek.admin.di

import com.coffeepeek.admin.locator.Constants
import com.coffeepeek.admin.locator.Locator
import com.coffeepeek.admin.theme.ThemeManager
import com.coffeepeek.admin.utils.CustomUrlFetcher
import com.coffeepeek.api.CoffeePeekClient
import com.coffeepeek.admin.ui.NavigatorViewModel
import com.coffeepeek.admin.ui.screen.auth.AuthViewModel
import com.coffeepeek.admin.ui.screen.auth.registr.RegisterViewModel
import com.coffeepeek.admin.ui.screen.feed.FeedViewModel
import com.coffeepeek.admin.ui.screen.addshop.AddShopViewModel
import com.coffeepeek.admin.ui.screen.editprofile.EditProfileViewModel
import com.coffeepeek.admin.ui.screen.map.MapViewModel
import com.coffeepeek.admin.ui.screen.profile.ProfileViewModel
import com.coffeepeek.admin.ui.screen.checkins.VisitedPlacesViewModel
import com.coffeepeek.admin.ui.screen.favorites.FavoritesViewModel
import com.coffeepeek.admin.ui.screen.review.CreateReviewViewModel
import com.coffeepeek.admin.ui.screen.review.EditReviewViewModel
import com.coffeepeek.admin.ui.screen.reviews.MyReviewsViewModel
import com.coffeepeek.admin.ui.screen.shop.ShopDetailViewModel
import com.coffeepeek.data.di.dataModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoin() {
    val database = Locator.database
    ThemeManager.initialize(database.settingRepository)

    startKoin {
        modules(
            dataModule(
                baseUrl = Constants.BASE_URL,
                cacheFolder = Locator.cacheFolder,
                database = database,
                debug = true,
            ),
            appModule(),
        )
    }
}

private fun appModule() = module {
    single<CustomUrlFetcher> { createImageUrlFetcher(get<CoffeePeekClient>().client) }
    factory { AuthViewModel(get()) }
    factory { RegisterViewModel(get()) }
    factory { NavigatorViewModel(get()) }
    factory { FeedViewModel(get(), get()) }
    factory { MapViewModel(get()) }
    factory { (shopId: String) -> ShopDetailViewModel(shopId, get(), get(), get()) }
    factory { ProfileViewModel(get(), get(), get()) }
    factory { AddShopViewModel(get()) }
    factory { EditProfileViewModel(get()) }
    factory { FavoritesViewModel(get()) }
    factory { MyReviewsViewModel(get(), get()) }
    factory { VisitedPlacesViewModel(get()) }
    factory { (shopId: String) -> CreateReviewViewModel(shopId, get()) }
    factory { (reviewId: String) -> EditReviewViewModel(reviewId, get(), get()) }
}
