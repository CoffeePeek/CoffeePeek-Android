package com.coffeepeek.admin.ui.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop
import com.coffeepeek.domain.model.ShopSchedule
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val shops: List<MapShop> = emptyList(),
    val selectedShop: MapShop? = null,
    val selectedShopDetails: CoffeeShopDetails? = null,
    val isLoadingShopDetails: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val cameraTarget: Pair<Double, Double>? = null,
    val cameraZoom: Float? = null,
)

class MapViewModel(
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    private var boundsJob: Job? = null
    private var detailsJob: Job? = null
    private var suppressBoundsUpdates = false

    fun onBoundsChanged(bounds: MapBounds) {
        if (suppressBoundsUpdates) return
        boundsJob?.cancel()
        boundsJob = viewModelScope.launch {
            delay(400)
            _state.update { it.copy(isLoading = true, error = null) }
            shopRepository.getShopsInBounds(bounds)
                .onSuccess { shops ->
                    _state.update { current ->
                        current.copy(
                            shops = mergeShops(current.shops, shops, current.selectedShop),
                            isLoading = false,
                            selectedShop = current.selectedShop?.let { selected ->
                                mergeShops(current.shops, shops, selected).find { it.id == selected.id }
                                    ?: selected
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

    fun onShopSelected(shop: MapShop) {
        selectShop(shop, moveCamera = true)
    }

    fun clearSelection() {
        detailsJob?.cancel()
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

    fun focusOnShop(focus: com.coffeepeek.admin.ui.Navigator.MapShopFocus) {
        val shop = MapShop(
            id = focus.shopId,
            title = focus.title,
            latitude = focus.latitude,
            longitude = focus.longitude,
        )
        viewModelScope.launch {
            suppressBoundsUpdates = true
            _state.update { current ->
                current.copy(
                    shops = (current.shops + shop).distinctBy { it.id },
                    cameraTarget = focus.latitude to focus.longitude,
                    cameraZoom = 16f,
                )
            }
            selectShop(shop, moveCamera = false)
            delay(900)
            suppressBoundsUpdates = false
        }
    }

    fun onCameraTargetApplied() {
        _state.update { it.copy(cameraTarget = null, cameraZoom = null) }
    }

    private fun selectShop(shop: MapShop, moveCamera: Boolean) {
        detailsJob?.cancel()
        if (moveCamera) {
            viewModelScope.launch {
                suppressBoundsUpdates = true
                _state.update {
                    it.copy(
                        selectedShop = shop,
                        selectedShopDetails = null,
                        isLoadingShopDetails = true,
                        shops = (it.shops + shop).distinctBy { item -> item.id },
                        cameraTarget = shop.latitude to shop.longitude,
                        cameraZoom = 16f,
                    )
                }
                delay(900)
                suppressBoundsUpdates = false
            }
        } else {
            _state.update {
                it.copy(
                    selectedShop = shop,
                    selectedShopDetails = null,
                    isLoadingShopDetails = true,
                    shops = (it.shops + shop).distinctBy { item -> item.id },
                )
            }
        }
        detailsJob = viewModelScope.launch {
            shopRepository.getShopDetails(shop.id)
                .onSuccess { details ->
                    _state.update { current ->
                        if (current.selectedShop?.id != shop.id) return@update current
                        current.copy(
                            selectedShopDetails = details,
                            isLoadingShopDetails = false,
                        )
                    }
                }
                .onFailure {
                    _state.update { current ->
                        if (current.selectedShop?.id != shop.id) return@update current
                        current.copy(isLoadingShopDetails = false)
                    }
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
