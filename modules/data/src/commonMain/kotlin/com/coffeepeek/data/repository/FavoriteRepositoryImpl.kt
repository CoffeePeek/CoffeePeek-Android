package com.coffeepeek.data.repository

import com.coffeepeek.data.local.LocalFavoriteShopDto
import com.coffeepeek.domain.model.CoffeeShop
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.repository.FavoriteRepository
import com.coffeepeek.room.DatabaseCore
import com.coffeepeek.room.repository.readSerializable
import com.coffeepeek.room.repository.saveSerializable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FavoriteRepositoryImpl(
    private val database: DatabaseCore,
) : FavoriteRepository {

    private val settings = database.settingRepository
    private val mutex = Mutex()

    override suspend fun getFavoriteIds(): Set<String> =
        readAll().map { it.id }.toSet()

    override suspend fun isFavorite(shopId: String): Boolean =
        readAll().any { it.id == shopId }

    override suspend fun getFavorites(): Result<List<CoffeeShopDetails>> = runCatching {
        readAll().map { it.toDomain() }
    }

    override suspend fun addFavorite(shop: CoffeeShop, address: String?): Result<Unit> = runCatching {
        mutex.withLock {
            val current = readAll().filterNot { it.id == shop.id }
            writeAll(listOf(LocalFavoriteShopDto.from(shop, address)) + current)
        }
    }

    override suspend fun removeFavorite(shopId: String): Result<Unit> = runCatching {
        mutex.withLock {
            writeAll(readAll().filterNot { it.id == shopId })
        }
    }

    override suspend fun clearAll() {
        mutex.withLock {
            writeAll(emptyList())
        }
    }

    private suspend fun readAll(): List<LocalFavoriteShopDto> =
        settings.readSerializable<List<LocalFavoriteShopDto>>(KEY).orEmpty()

    private suspend fun writeAll(items: List<LocalFavoriteShopDto>) {
        if (items.isEmpty()) {
            settings.saveSerializable<List<LocalFavoriteShopDto>>(KEY, null)
        } else {
            settings.saveSerializable(KEY, items)
        }
    }

    private companion object {
        const val KEY = "local_favorite_shops"
    }
}
