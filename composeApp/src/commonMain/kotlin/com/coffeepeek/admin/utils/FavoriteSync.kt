package com.coffeepeek.admin.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object FavoriteSync {

    private val _changes = MutableSharedFlow<FavoriteChange>(extraBufferCapacity = 1)
    val changes = _changes.asSharedFlow()

    suspend fun notifyChanged(shopId: String, isFavorite: Boolean) {
        _changes.emit(FavoriteChange(shopId, isFavorite))
    }

    data class FavoriteChange(val shopId: String, val isFavorite: Boolean)
}
