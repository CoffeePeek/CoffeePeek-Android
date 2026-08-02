package com.coffeepeek.admin.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ReviewSync {

    private val _changes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val changes = _changes.asSharedFlow()

    fun notifyChanged(shopId: String) {
        _changes.tryEmit(shopId)
    }
}
