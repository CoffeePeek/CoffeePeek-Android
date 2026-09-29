package com.coffeepeek.feature.favorites.impl.ui

import com.coffeepeek.feature.favorites.domain.FavoriteShop

internal data class FavoritesUiState(
    val shops: List<FavoriteShop> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val actionFailed: Boolean = false,
    val removing: Set<String> = emptySet(),
)
