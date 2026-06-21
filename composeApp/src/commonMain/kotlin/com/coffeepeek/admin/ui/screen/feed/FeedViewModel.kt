package com.coffeepeek.admin.ui.screen.feed

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.utils.FavoriteSync
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.City
import com.coffeepeek.domain.model.CoffeeShop
import com.coffeepeek.domain.model.ShopFilters
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedFiltersUi(
    val cityId: String? = null,
    val priceRange: Int? = null,
    val minRating: Double? = null,
    val roasterIds: Set<String> = emptySet(),
    val beanIds: Set<String> = emptySet(),
    val equipmentIds: Set<String> = emptySet(),
    val brewMethodIds: Set<String> = emptySet(),
)

data class FeedUiState(
    val shops: List<CoffeeShop> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val filters: FeedFiltersUi = FeedFiltersUi(),
    val cities: List<City> = emptyList(),
    val beans: List<CatalogItem> = emptyList(),
    val equipment: List<CatalogItem> = emptyList(),
    val roasters: List<CatalogItem> = emptyList(),
    val brewMethods: List<CatalogItem> = emptyList(),
    val showFilters: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false,
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (filters.cityId != null) count++
            if (filters.priceRange != null) count++
            if (filters.minRating != null) count++
            count += filters.roasterIds.size + filters.beanIds.size +
                filters.equipmentIds.size + filters.brewMethodIds.size
            return count
        }
}

@OptIn(FlowPreview::class)
class FeedViewModel(
    private val shopRepository: ShopRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        loadCatalogs()
        loadShops(reset = true)
        queryFlow
            .debounce(400)
            .distinctUntilChanged()
            .onEach { query ->
                _uiState.update { it.copy(query = query) }
                loadShops(reset = true)
            }
            .launchIn(workScope)

        FavoriteSync.changes
            .onEach { change ->
                _uiState.update { state ->
                    state.copy(
                        shops = state.shops.map { shop ->
                            if (shop.id == change.shopId) {
                                shop.copy(isFavorite = change.isFavorite)
                            } else {
                                shop
                            }
                        },
                    )
                }
            }
            .launchIn(workScope)
    }

    private fun loadCatalogs() {
        workScope.launch {
            shopRepository.getCatalogs()
                .onSuccess { catalogs ->
                    _uiState.update {
                        it.copy(
                            cities = catalogs.cities,
                            beans = catalogs.beans,
                            equipment = catalogs.equipment,
                            roasters = catalogs.roasters,
                            brewMethods = catalogs.brewMethods,
                        )
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        queryFlow.value = query
    }

    fun toggleFilters() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }

    fun setCity(cityId: String?) {
        _uiState.update { it.copy(filters = it.filters.copy(cityId = cityId)) }
        loadShops(reset = true)
    }

    fun setPriceRange(priceRange: Int?) {
        _uiState.update { it.copy(filters = it.filters.copy(priceRange = priceRange)) }
        loadShops(reset = true)
    }

    fun setMinRating(rating: Double?) {
        _uiState.update { it.copy(filters = it.filters.copy(minRating = rating)) }
        loadShops(reset = true)
    }

    fun toggleFilterCatalog(
        type: String,
        id: String,
    ) {
        _uiState.update { state ->
            val filters = when (type) {
                "roaster" -> state.filters.copy(
                    roasterIds = state.filters.roasterIds.toggle(id),
                )
                "bean" -> state.filters.copy(
                    beanIds = state.filters.beanIds.toggle(id),
                )
                "equipment" -> state.filters.copy(
                    equipmentIds = state.filters.equipmentIds.toggle(id),
                )
                "brew" -> state.filters.copy(
                    brewMethodIds = state.filters.brewMethodIds.toggle(id),
                )
                else -> state.filters
            }
            state.copy(filters = filters)
        }
        loadShops(reset = true)
    }

    fun clearFilters() {
        _uiState.update { it.copy(filters = FeedFiltersUi()) }
        loadShops(reset = true)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return
        loadShops(reset = false)
    }

    fun refresh() = loadShops(reset = true)

    private fun loadShops(reset: Boolean) {
        val state = _uiState.value
        if (reset && state.isLoading) return
        val page = if (reset) 1 else state.currentPage + 1

        workScope.launch {
            if (reset) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            val filters = state.filters
            shopRepository.searchShops(
                ShopFilters(
                    query = state.query.takeIf { it.isNotBlank() },
                    cityId = filters.cityId,
                    roasterIds = filters.roasterIds.toList(),
                    beanIds = filters.beanIds.toList(),
                    equipmentIds = filters.equipmentIds.toList(),
                    brewMethodIds = filters.brewMethodIds.toList(),
                    priceRange = filters.priceRange,
                    minRating = filters.minRating,
                    page = page,
                    pageSize = 20,
                )
            ).onSuccess { result ->
                _uiState.update { s ->
                    s.copy(
                        shops = if (reset) result.items else s.shops + result.items,
                        currentPage = result.currentPage,
                        totalPages = result.totalPages,
                        hasMore = result.currentPage < result.totalPages,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = e.message) }
            }
        }
    }

    private fun Set<String>.toggle(id: String): Set<String> =
        if (contains(id)) this - id else this + id
}
