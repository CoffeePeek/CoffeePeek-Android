package com.coffeepeek.admin.ui.screen.roaster

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.location.GeoPoint
import com.coffeepeek.admin.location.NEARBY_RADIUS_METERS
import com.coffeepeek.admin.location.distanceToShopMeters
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.RoasterDetails
import com.coffeepeek.domain.model.ShopLocation
import com.coffeepeek.domain.repository.RoasterRepository
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal data class RoasterListItem(val catalog: CatalogItem, val details: RoasterDetails? = null) {
    val routeId: String? get() = catalog.address?.slug
    fun distance(origin: GeoPoint?): Double? = details?.location?.let {
        distanceToShopMeters(origin, ShopLocation(it.address, it.latitude, it.longitude))
    }
}

internal data class RoasterListUiState(
    val items: List<RoasterListItem> = emptyList(),
    val query: String = "",
    val nearbyOnly: Boolean = false,
    val popularFirst: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    fun visibleItems(origin: GeoPoint?): List<RoasterListItem> {
        val matching = items.filter {
            it.catalog.name.contains(query.trim(), ignoreCase = true) &&
                (!nearbyOnly || (it.distance(origin)?.let { distance -> distance <= NEARBY_RADIUS_METERS } == true))
        }
        return when {
            popularFirst -> matching.sortedByDescending { it.details?.shops?.size ?: -1 }
            nearbyOnly -> matching.sortedBy { it.distance(origin) }
            else -> matching
        }
    }
}

internal class RoasterListViewModel(
    private val shops: ShopRepository,
    private val roasters: RoasterRepository,
) : BaseViewModel() {
    private val _state = MutableStateFlow(RoasterListUiState())
    val state = _state.asStateFlow()
    private var loadJob: Job? = null

    init { refresh() }

    fun onQueryChange(query: String) { _state.update { it.copy(query = query) } }
    fun toggleNearby() { _state.update { it.copy(nearbyOnly = !it.nearbyOnly) } }
    fun togglePopular() { _state.update { it.copy(popularFirst = !it.popularFirst) } }
    fun resetFilters() { _state.update { it.copy(nearbyOnly = false, popularFirst = false) } }

    fun refresh() {
        loadJob?.cancel()
        loadJob = workScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = shops.getCatalogs()
            currentCoroutineContext().ensureActive()
            result.onSuccess { catalogs ->
                _state.update { it.copy(items = catalogs.roasters.map(::RoasterListItem)) }
                // ponytail: catalog + one detail request per roaster; use a summary endpoint when the catalog grows.
                val requests = Semaphore(4)
                catalogs.roasters.map { catalog ->
                    async {
                        requests.withPermit {
                            val routeId = catalog.address?.slug ?: return@withPermit
                            val details = roasters.getRoaster(routeId).getOrNull()
                            currentCoroutineContext().ensureActive()
                            _state.update { state ->
                                state.copy(
                                    items = state.items.map { if (it.catalog.id == catalog.id) it.copy(details = details) else it },
                                    error = if (details == null) "Некоторые сведения об обжарщиках не удалось загрузить" else state.error,
                                )
                            }
                        }
                    }
                }.awaitAll()
                _state.update { it.copy(isLoading = false) }
            }.onFailure {
                _state.update { it.copy(isLoading = false, error = "Не удалось загрузить обжарщиков") }
            }
        }
    }
}
