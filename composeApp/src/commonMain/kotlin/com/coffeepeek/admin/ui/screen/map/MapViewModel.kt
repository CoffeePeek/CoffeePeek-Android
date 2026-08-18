package com.coffeepeek.admin.ui.screen.map

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.City
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop
import com.coffeepeek.domain.model.ShopFilters
import com.coffeepeek.domain.model.ShopSchedule
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapFiltersUi(
    val cityId: String? = null,
    val priceRange: Int? = null,
    val minRating: Double? = null,
    val roasterIds: Set<String> = emptySet(),
    val beanIds: Set<String> = emptySet(),
    val equipmentIds: Set<String> = emptySet(),
    val brewMethodIds: Set<String> = emptySet(),
    val tagIds: Set<String> = emptySet(),
)

data class MapUiState(
    val shops: List<MapShop> = emptyList(),
    val selectedShop: MapShop? = null,
    val selectedShopDetails: CoffeeShopDetails? = null,
    val isLoadingShopDetails: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val filters: MapFiltersUi = MapFiltersUi(),
    val cities: List<City> = emptyList(),
    val beans: List<CatalogItem> = emptyList(),
    val equipment: List<CatalogItem> = emptyList(),
    val roasters: List<CatalogItem> = emptyList(),
    val brewMethods: List<CatalogItem> = emptyList(),
    val shopTags: List<CatalogItem> = emptyList(),
    val showFilters: Boolean = false,
    val showSearchArea: Boolean = false,
    val activeBounds: MapBounds? = null,
    val pendingBounds: MapBounds? = null,
    val myLocationRequest: Int = 0,
    val cameraTarget: Pair<Double, Double>? = null,
    val cameraZoom: Float? = null,
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (query.isNotBlank()) count++
            if (filters.cityId != null) count++
            if (filters.priceRange != null) count++
            if (filters.minRating != null) count++
            count += filters.roasterIds.size + filters.beanIds.size +
                filters.equipmentIds.size + filters.brewMethodIds.size + filters.tagIds.size
            return count
        }
}

class MapViewModel(
    private val shopRepository: ShopRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    private var boundsJob: Job? = null
    private var detailsJob: Job? = null
    private var boundsPauseJob: Job? = null
    private var selectionVersion = 0
    private val detailsCache = mutableMapOf<String, CoffeeShopDetails>()
    private var suppressBoundsUpdates = false

    init {
        loadCatalogs()
        requestMyLocation()
    }

    fun onBoundsChanged(bounds: MapBounds) {
        if (suppressBoundsUpdates) return
        boundsJob?.cancel()
        val shouldLoadImmediately = _state.value.activeBounds == null && _state.value.shops.isEmpty()
        _state.update {
            it.copy(
                pendingBounds = bounds,
                showSearchArea = !shouldLoadImmediately,
            )
        }
        if (shouldLoadImmediately) {
            loadBounds(bounds)
        }
    }

    fun searchCurrentArea() {
        val bounds = _state.value.pendingBounds ?: _state.value.activeBounds ?: return
        loadBounds(bounds)
    }

    fun onShopSelected(shop: MapShop) {
        if (_state.value.selectedShop?.id == shop.id) return
        selectShop(shop)
    }

    fun clearSelection() {
        detailsJob?.cancel()
        boundsPauseJob?.cancel()
        suppressBoundsUpdates = false
        selectionVersion++
        _state.update {
            it.copy(
                selectedShop = null,
                selectedShopDetails = null,
                isLoadingShopDetails = false,
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun toggleFilters() {
        _state.update { it.copy(showFilters = !it.showFilters) }
    }

    fun dismissFilters() {
        _state.update { it.copy(showFilters = false) }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun setCity(cityId: String?) {
        _state.update { it.copy(filters = it.filters.copy(cityId = cityId)) }
    }

    fun setPriceRange(priceRange: Int?) {
        _state.update { it.copy(filters = it.filters.copy(priceRange = priceRange)) }
    }

    fun setMinRating(rating: Double?) {
        _state.update { it.copy(filters = it.filters.copy(minRating = rating)) }
    }

    fun toggleFilterCatalog(type: String, id: String) {
        _state.update { state ->
            val filters = when (type) {
                "roaster" -> state.filters.copy(roasterIds = state.filters.roasterIds.toggle(id))
                "bean" -> state.filters.copy(beanIds = state.filters.beanIds.toggle(id))
                "equipment" -> state.filters.copy(equipmentIds = state.filters.equipmentIds.toggle(id))
                "brew" -> state.filters.copy(brewMethodIds = state.filters.brewMethodIds.toggle(id))
                "tag" -> state.filters.copy(tagIds = state.filters.tagIds.toggle(id))
                else -> state.filters
            }
            state.copy(filters = filters)
        }
    }

    fun applyFilters() {
        _state.update { it.copy(showFilters = false) }
        searchCurrentArea()
    }

    fun clearFilters() {
        _state.update { it.copy(query = "", filters = MapFiltersUi()) }
        searchCurrentArea()
    }

    fun requestMyLocation() {
        _state.update { it.copy(myLocationRequest = it.myLocationRequest + 1) }
    }

    fun focusOnShop(focus: com.coffeepeek.admin.ui.Navigator.MapShopFocus) {
        val shop = MapShop(
            id = focus.shopId,
            title = focus.title,
            latitude = focus.latitude,
            longitude = focus.longitude,
        )
        workScope.launch {
            _state.update { current ->
                current.copy(
                    shops = (current.shops + shop).distinctBy { it.id },
                    cameraTarget = focus.latitude to focus.longitude,
                    cameraZoom = 16f,
                )
            }
            selectShop(shop)
            pauseBoundsUpdates(700)
        }
    }

    fun onCameraTargetApplied() {
        _state.update { it.copy(cameraTarget = null, cameraZoom = null) }
    }

    fun onMyLocationApplied(latitude: Double, longitude: Double) {
        _state.update {
            it.copy(
                cameraTarget = latitude to longitude,
                cameraZoom = 15f,
            )
        }
        pauseBoundsUpdates(700)
    }

    fun onLocationPermissionDenied() {
        _state.update {
            it.copy(error = "Нет доступа к геолокации. Разрешите в настройках приложения.")
        }
    }

    private fun loadCatalogs() {
        workScope.launch {
            shopRepository.getCatalogs()
                .onSuccess { catalogs ->
                    _state.update {
                        it.copy(
                            cities = catalogs.cities,
                            beans = catalogs.beans,
                            equipment = catalogs.equipment,
                            roasters = catalogs.roasters,
                            brewMethods = catalogs.brewMethods,
                            shopTags = catalogs.shopTags,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(error = err.message ?: "Ошибка загрузки каталогов")
                    }
                }
        }
    }

    private fun loadBounds(bounds: MapBounds) {
        boundsJob?.cancel()
        boundsJob = workScope.launch {
            delay(250)
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    activeBounds = bounds,
                    pendingBounds = bounds,
                    showSearchArea = false,
                )
            }
            val state = _state.value
            val filters = state.filters
            shopRepository.getShopsInBounds(
                bounds = bounds,
                filters = ShopFilters(
                    query = state.query.takeIf { it.isNotBlank() },
                    cityId = filters.cityId,
                    roasterIds = filters.roasterIds.toList(),
                    beanIds = filters.beanIds.toList(),
                    equipmentIds = filters.equipmentIds.toList(),
                    brewMethodIds = filters.brewMethodIds.toList(),
                    tagIds = filters.tagIds.toList(),
                    priceRange = filters.priceRange,
                    minRating = filters.minRating,
                ),
            )
                .onSuccess { shops ->
                    _state.update { current ->
                        val merged = mergeShops(current.shops, shops, current.selectedShop)
                        current.copy(
                            shops = merged,
                            isLoading = false,
                            selectedShop = current.selectedShop?.let { selected ->
                                merged.find { it.id == selected.id } ?: selected
                            },
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = err.message ?: "Ошибка загрузки кофеен",
                        )
                    }
                }
        }
    }

    private fun selectShop(shop: MapShop) {
        detailsJob?.cancel()
        val version = ++selectionVersion
        val cachedDetails = detailsCache[shop.id]

        _state.update {
            it.copy(
                selectedShop = shop,
                selectedShopDetails = cachedDetails,
                isLoadingShopDetails = cachedDetails == null,
                shops = if (it.shops.any { item -> item.id == shop.id }) {
                    it.shops
                } else {
                    it.shops + shop
                },
            )
        }

        if (cachedDetails != null) return

        detailsJob = workScope.launch {
            shopRepository.getShopDetails(shop.id)
                .onSuccess { details ->
                    detailsCache[shop.id] = details
                    _state.update { current ->
                        if (version != selectionVersion || current.selectedShop?.id != shop.id) return@update current
                        current.copy(
                            selectedShopDetails = details,
                            isLoadingShopDetails = false,
                        )
                    }
                }
                .onFailure {
                    _state.update { current ->
                        if (version != selectionVersion || current.selectedShop?.id != shop.id) return@update current
                        current.copy(isLoadingShopDetails = false)
                    }
                }
        }
    }

    private fun pauseBoundsUpdates(durationMs: Long = 600) {
        boundsPauseJob?.cancel()
        boundsPauseJob = workScope.launch {
            suppressBoundsUpdates = true
            try {
                delay(durationMs)
            } finally {
                suppressBoundsUpdates = false
            }
        }
    }

    private fun mergeShops(
        current: List<MapShop>,
        loaded: List<MapShop>,
        pinned: MapShop?,
    ): List<MapShop> {
        val merged = (current + loaded).toMutableList()
        pinned?.let { shop ->
            if (merged.none { it.id == shop.id }) merged.add(shop)
        }
        return merged.distinctBy { it.id }
    }

    private fun Set<String>.toggle(id: String): Set<String> =
        if (contains(id)) this - id else this + id
}

internal fun formatMapHoursSummary(schedules: List<ShopSchedule>): String? {
    val openSchedule = schedules.firstOrNull { !it.isClosed && it.intervals.isNotEmpty() }
        ?: schedules.firstOrNull()
        ?: return null

    if (openSchedule.isClosed) return "Закрыто"

    val interval = openSchedule.intervals.firstOrNull() ?: return null
    val open = interval.openTime.split(":").take(2).joinToString(":")
    val close = interval.closeTime.split(":").take(2).joinToString(":")
    return "$open – $close"
}
