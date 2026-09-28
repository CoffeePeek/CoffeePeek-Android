package com.coffeepeek.feature.favorites.di

import com.coffeepeek.feature.favorites.api.FavoritesEntry
import com.coffeepeek.core.coroutines.DefaultDispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import com.coffeepeek.feature.favorites.data.FavoritesStorage
import com.coffeepeek.feature.favorites.data.createFavoritesRepository
import com.coffeepeek.feature.favorites.domain.FavoritesRepository
import com.coffeepeek.feature.favorites.domain.ObserveFavoriteIdsUseCase
import com.coffeepeek.feature.favorites.impl.createFavoritesEntry
import com.coffeepeek.room.model.Setting
import com.coffeepeek.room.repository.SettingRepository
import com.coffeepeek.domain.repository.FavoriteRepository as LegacyFavoriteRepository
import kotlinx.coroutines.flow.map
import org.koin.core.module.Module
import org.koin.dsl.module

/** One repo per session/storage. Application explicitly loads this module; no global Koin start. */
fun favoritesModule(storage: FavoritesStorage, dispatcher: CoroutineDispatcher = DefaultDispatcherProvider.io): Module = module {
    single<FavoritesRepository> { createFavoritesRepository(storage, dispatcher) }
    factory { ObserveFavoriteIdsUseCase(get()) }
    single<FavoritesEntry> { createFavoritesEntry(get()) }
}

/** Temporary composition bridge. Does not construct/open the old database or register old bindings. */
fun favoritesRoomModule(settings: SettingRepository, dispatcher: CoroutineDispatcher = DefaultDispatcherProvider.io): Module =
    favoritesModule(RoomFavoritesStorage(settings), dispatcher)

/** Load only when the old application binding is omitted: both contracts then share one writer. */
fun legacyFavoritesConsumersModule(): Module = module {
    single<LegacyFavoriteRepository> { createLegacyFavoritesRepositoryBridge(get()) }
}

internal class RoomFavoritesStorage(private val settings: SettingRepository) : FavoritesStorage {
    override suspend fun read(key: String) = settings.read(key)?.value
    override fun observe(key: String) = settings.readFlow(key).map { it?.value }
    override suspend fun write(key: String, value: String?) {
        if (value == null) settings.delete(key) else settings.save(Setting(key, value))
    }
}
