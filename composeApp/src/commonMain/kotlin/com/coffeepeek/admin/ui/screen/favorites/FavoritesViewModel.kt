package com.coffeepeek.admin.ui.screen.favorites

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.utils.FavoriteSync
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val shops: List<CoffeeShopDetails> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state = _state.asStateFlow()

    init {
        load()
        FavoriteSync.changes
            .onEach { change ->
                if (!change.isFavorite) {
                    _state.update { state ->
                        state.copy(shops = state.shops.filter { it.shop.id != change.shopId })
                    }
                }
            }
            .launchIn(workScope)
    }

    fun load() {
        workScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            favoriteRepository.getFavorites()
                .onSuccess { shops -> _state.update { it.copy(shops = shops, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
