package com.coffeepeek.data.repository

import com.coffeepeek.api.service.FavoriteApiService
import com.coffeepeek.data.mapper.ShopMapper.toDomain
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.repository.FavoriteRepository

class FavoriteRepositoryImpl(
    private val favoriteApiService: FavoriteApiService,
) : FavoriteRepository {

    override suspend fun getFavorites(): Result<List<CoffeeShopDetails>> =
        favoriteApiService.getFavorites().map { response ->
            response.favoriteShops.map { it.toDomain() }
        }

    override suspend fun addFavorite(shopId: String): Result<Unit> =
        favoriteApiService.addFavorite(shopId)

    override suspend fun removeFavorite(shopId: String): Result<Unit> =
        favoriteApiService.removeFavorite(shopId)
}
