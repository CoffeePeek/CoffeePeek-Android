package com.coffeepeek.admin.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BasePager<T>(
    private val load: suspend (Int) -> Result<List<T>>,
    private val pageSize: Int = 20,
    val scope: CoroutineScope
) {

    private var loaded: MutableMap<Int, T> = mutableMapOf()

    private val _data = MutableStateFlow<List<T>>(emptyList())
    val data = _data.asStateFlow()

    private val _page = MutableStateFlow<Int?>(null)
    val page = _page.asStateFlow()

    private val _isRefreshing = MutableStateFlow(true)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        _page.filterNotNull()
            .onEach { page ->
                load(page)
                    .onSuccess { appendData(page, it) }
                    .onFailure { _isRefreshing.value = false }
            }.launchIn(scope)

        _page.filter { it == null }
            .onEach { scope.launch { _page.value = 0 } }
            .launchIn(scope)
    }

    private fun appendData(page: Int, data: List<T>) {
        val snapshot = HashMap(loaded)
        val start = page * pageSize
        data.forEachIndexed { i, item -> snapshot[i + start] = item }
        loaded = snapshot
        _data.value = snapshot.values.toList()
        _isRefreshing.value = false
    }

    fun position(position: Int) {
        val page = (position + pageSize / 2) / pageSize
        if (page > (_page.value ?: 0)) _page.value = page
    }

    fun refresh() {
        _isRefreshing.value = true
        loaded = HashMap()
        _page.value = null
    }

}

fun <T> Flow<*>.flatMapPager(scope: CoroutineScope, pager: () -> BasePager<T>): StateFlow<BasePager<T>> {
    return map { pager() }.stateIn(
        scope,
        SharingStarted.Eagerly,
        BasePager(
            load = { runCatching { emptyList() } },
            scope = scope
        )
    )
}