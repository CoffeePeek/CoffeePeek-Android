package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.CoffeeShop
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.CreateShopInput
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop
import com.coffeepeek.domain.model.PagedResult
import com.coffeepeek.domain.model.ShopCatalogs
import com.coffeepeek.domain.model.ShopFilters

interface ShopRepository {
    suspend fun searchShops(filters: ShopFilters): Result<PagedResult<CoffeeShop>>
    suspend fun getShopDetails(id: String): Result<CoffeeShopDetails>
    suspend fun getShopsInBounds(bounds: MapBounds, filters: ShopFilters = ShopFilters()): Result<List<MapShop>>
    suspend fun getCatalogs(): Result<ShopCatalogs>
    suspend fun createShop(input: CreateShopInput): Result<Unit>
}
