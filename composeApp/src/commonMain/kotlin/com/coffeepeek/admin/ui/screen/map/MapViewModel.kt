package com.coffeepeek.admin.ui.screen.map

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.settings.CityPreference
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.City
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapCluster
import com.coffeepeek.domain.model.MapCoffeeZone
import com.coffeepeek.domain.model.MapShop
import com.coffeepeek.domain.model.ShopFilters
import com.coffeepeek.domain.model.ShopSchedule
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapFiltersUi(
    val cityId: String? = null,
    val coffeeFocus: String? = null,
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
    val clusters: List<MapCluster> = emptyList(),
    val zones: List<MapCoffeeZone> = emptyList(),
    val showZones: Boolean = true,
    val selectedShop: MapShop? = null,
    val selectedZone: MapCoffeeZone? = null,
    val selectedShopDetails: CoffeeShopDetails? = null,
    val isLoadingShopDetails: Boolean = false,
    val isLoading: Boolean = false,
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
    val activeZoom: Float? = null,
    val pendingZoom: Float? = null,
    val isTruncated: Boolean = false,
    val myLocationRequest: Int = 0,
    val cameraTarget: Pair<Double, Double>? = null,
    val cameraZoom: Float? = null,
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (query.isNotBlank()) count++
            if (filters.coffeeFocus != null) count++
            if (filters.priceRange != null) count++
            if (filters.minRating != null) count++
            count += filters.roasterIds.size + filters.beanIds.size +
                filters.equipmentIds.size + filters.brewMethodIds.size + filters.tagIds.size
            return count
        }
}

class MapViewModel(
    private val shopRepository: ShopRepository,
    private val cityPreference: CityPreference,
) : BaseViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    private var boundsJob: Job? = null
    private var queryJob: Job? = null
    private var detailsJob: Job? = null
    private var boundsPauseJob: Job? = null
    private var selectionVersion = 0
    private val detailsCache = mutableMapOf<String, CoffeeShopDetails>()
    private var suppressBoundsUpdates = false
    private var isCityReady = false
    private var zonesCityId: String? = null

    init {
        loadCatalogs()
        cityPreference.selectedCityId
            .onEach { cityId ->
                if (!isCityReady || cityId == null || cityId == _state.value.filters.cityId) {
                    return@onEach
                }
                _state.update { it.copy(filters = it.filters.copy(cityId = cityId)) }
                searchCurrentArea()
            }
            .launchIn(workScope)
    }

    fun onBoundsChanged(bounds: MapBounds, zoom: Float) {
        if (suppressBoundsUpdates) return
        boundsJob?.cancel()
        _state.update {
            it.copy(
                pendingBounds = bounds,
                pendingZoom = zoom,
                showSearchArea = false,
            )
        }
        loadBounds(bounds, zoom)
    }

    fun searchCurrentArea() {
        val bounds = _state.value.pendingBounds ?: _state.value.activeBounds ?: return
        val zoom = _state.value.pendingZoom ?: _state.value.activeZoom ?: return
        loadBounds(bounds, zoom)
    }

    fun onShopSelected(shop: MapShop) {
        if (_state.value.selectedShop?.id == shop.id) return
        _state.update { it.copy(selectedZone = null) }
        selectShop(shop)
    }

    fun onZoneSelected(zone: MapCoffeeZone) {
        detailsJob?.cancel()
        _state.update {
            it.copy(
                selectedZone = zone,
                selectedShop = null,
                selectedShopDetails = null,
                isLoadingShopDetails = false,
            )
        }
    }

    fun showSelectedZoneShops() {
        val zone = _state.value.selectedZone ?: return
        _state.update {
            it.copy(
                selectedZone = null,
                cameraTarget = zone.latitude to zone.longitude,
                cameraZoom = 14.5f,
            )
        }
        pauseBoundsUpdates(700)
    }

    fun toggleZones() {
        _state.update { it.copy(showZones = !it.showZones, selectedZone = null) }
    }

    fun clearZoneSelection() {
        _state.update { it.copy(selectedZone = null) }
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

    fun toggleFilters() {
        _state.update { it.copy(showFilters = !it.showFilters) }
    }

    fun dismissFilters() {
        _state.update { it.copy(showFilters = false) }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        queryJob?.cancel()
        queryJob = workScope.launch {
            delay(350)
            val bounds = _state.value.activeBounds ?: _state.value.pendingBounds ?: return@launch
            val zoom = _state.value.activeZoom ?: _state.value.pendingZoom ?: return@launch
            loadBounds(bounds, zoom)
        }
    }

    fun setCoffeeFocus(coffeeFocus: String?) {
        _state.update { it.copy(filters = it.filters.copy(coffeeFocus = coffeeFocus)) }
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
        _state.update {
            it.copy(query = "", filters = MapFiltersUi(cityId = it.filters.cityId))
        }
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

    private fun loadCatalogs() {
        workScope.launch {
            shopRepository.getCatalogs()
                .onSuccess { catalogs ->
                    val cityId = cityPreference.resolve(catalogs.cities)
                    _state.update {
                        it.copy(
                            filters = it.filters.copy(cityId = cityId),
                            cities = catalogs.cities,
                            beans = catalogs.beans,
                            equipment = catalogs.equipment,
                            roasters = catalogs.roasters,
                            brewMethods = catalogs.brewMethods,
                            shopTags = catalogs.shopTags,
                        )
                    }
                    isCityReady = true
                    val current = _state.value
                    val bounds = current.pendingBounds
                    val zoom = current.pendingZoom
                    if (bounds != null && zoom != null) loadBounds(bounds, zoom)
                }
                .onFailure {
                    isCityReady = true
                    val current = _state.value
                    val bounds = current.pendingBounds
                    val zoom = current.pendingZoom
                    if (bounds != null && zoom != null) loadBounds(bounds, zoom)
                }
        }
    }

    private fun loadBounds(bounds: MapBounds, zoom: Float) {
        if (!isCityReady) return
        boundsJob?.cancel()
        boundsJob = workScope.launch {
            delay(250)
            _state.update {
                it.copy(
                    isLoading = true,
                    activeBounds = bounds,
                    pendingBounds = bounds,
                    activeZoom = zoom,
                    pendingZoom = zoom,
                    showSearchArea = false,
                )
            }
            val state = _state.value
            val filters = state.filters
            val result = shopRepository.getMapContent(
                bounds = bounds,
                zoom = zoom,
                filters = ShopFilters(
                    query = state.query.takeIf { it.isNotBlank() },
                    cityId = filters.cityId,
                    coffeeFocus = filters.coffeeFocus,
                    roasterIds = filters.roasterIds.toList(),
                    beanIds = filters.beanIds.toList(),
                    equipmentIds = filters.equipmentIds.toList(),
                    brewMethodIds = filters.brewMethodIds.toList(),
                    tagIds = filters.tagIds.toList(),
                    priceRange = filters.priceRange,
                    minRating = filters.minRating,
                ),
            )
            currentCoroutineContext().ensureActive()
            result.onSuccess { content ->
                    _state.update { current ->
                        val merged = mergeShops(content.shops, current.selectedShop)
                        // The API only returns zones for the current viewport, so zooming in used to drop
                        // them. Keep every zone seen for this city; fresh data wins. Reset on city change.
                        val knownZones = if (zonesCityId == filters.cityId) current.zones else emptyList()
                        zonesCityId = filters.cityId
                        val zones = (content.zones + knownZones).distinctBy { it.id }
                        current.copy(
                            shops = merged,
                            clusters = content.clusters,
                            zones = zones,
                            isTruncated = content.isTruncated,
                            isLoading = false,
                            selectedShop = current.selectedShop?.let { selected ->
                                merged.find { it.id == selected.id } ?: selected
                            },
                            selectedZone = current.selectedZone?.takeIf { selected ->
                                zones.any { it.id == selected.id }
                            },
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showSearchArea = true,
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
        loaded: List<MapShop>,
        pinned: MapShop?,
    ): List<MapShop> = (loaded + listOfNotNull(pinned)).distinctBy { it.id }

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
