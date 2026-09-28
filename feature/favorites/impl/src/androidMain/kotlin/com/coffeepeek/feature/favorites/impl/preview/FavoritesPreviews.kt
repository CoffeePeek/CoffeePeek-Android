package com.coffeepeek.feature.favorites.impl.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.coffeepeek.core.designsystem.theme.CoffeePeekTheme
import com.coffeepeek.feature.favorites.domain.FavoriteShop
import com.coffeepeek.feature.favorites.impl.ui.FavoritesScreen
import com.coffeepeek.feature.favorites.impl.ui.FavoritesUiState

@Composable
private fun Sample(state: FavoritesUiState) = CoffeePeekTheme {
    FavoritesScreen(state, {}, {}, {}, {}, { _, _ -> "1 км" })
}

@PreviewLightDark @Composable
private fun FavoritesContentPreview() = Sample(FavoritesUiState(
    shops = listOf(FavoriteShop("sample", "Любимая кофейня", rating = 4.8, reviewCount = 12,
        cityName = "Минск", address = "Улица Кофейная, 1", latitude = 53.9, longitude = 27.56,
        isOpen = true, priceRange = "\$\$", tags = listOf("Спешелти"),
        brewMethods = listOf("Эспрессо", "Фильтр"))),
    isLoading = false,
))

@PreviewLightDark @Composable
private fun FavoritesClosedPreview() = Sample(FavoritesUiState(
    shops = listOf(FavoriteShop("closed", "Кофейня с очень длинным названием без фото",
        cityName = "Минск", isOpen = false, brewMethods = listOf("Пуровер"))),
    isLoading = false,
))

@PreviewLightDark @Composable
private fun FavoritesLoadingPreview() = Sample(FavoritesUiState())

@PreviewLightDark @Composable
private fun FavoritesEmptyPreview() = Sample(FavoritesUiState(isLoading = false))

@PreviewLightDark @Composable
private fun FavoritesErrorPreview() = Sample(FavoritesUiState(isLoading = false, loadFailed = true))
