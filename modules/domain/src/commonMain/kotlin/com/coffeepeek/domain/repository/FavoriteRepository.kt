package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.CoffeeShopDetails

interface FavoriteRepository {
    suspend fun getFavorites(): Result<List<CoffeeShopDetails>>
    suspend fun addFavorite(shopId: String): Result<Unit>
    suspend fun removeFavorite(shopId: String): Result<Unit>
}
