package com.coffeepeek.admin.ui.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop
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
    val isLoading: Boolean = false,
    val error: String? = null,
)

class MapViewModel(
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    private var boundsJob: Job? = null

    fun onBoundsChanged(bounds: MapBounds) {
        boundsJob?.cancel()
        boundsJob = viewModelScope.launch {
            delay(400)
            _state.update { it.copy(isLoading = true, error = null) }
            shopRepository.getShopsInBounds(bounds)
                .onSuccess { shops ->
                    _state.update { current ->
                        current.copy(
                            shops = shops,
                            isLoading = false,
                            selectedShop = current.selectedShop?.let { selected ->
                                shops.find { it.id == selected.id }
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
        _state.update { it.copy(selectedShop = shop) }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedShop = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
