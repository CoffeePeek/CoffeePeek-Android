package com.coffeepeek.admin.ui.screen.favorites

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.utils.FavoriteSync
import com.coffeepeek.domain.model.CoffeeShop
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
            .onEach { load(force = true) }
            .launchIn(workScope)
    }

    fun load(force: Boolean = false) {
        if (!force && (_state.value.isLoading || _state.value.shops.isNotEmpty())) return
        workScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            favoriteRepository.getFavorites()
                .onSuccess { shops -> _state.update { it.copy(shops = shops, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    /** Tapping the heart on a favorites card unfavorites it and drops it from the list. */
    fun removeFavorite(shop: CoffeeShop) {
        workScope.launch {
            _state.update { it.copy(shops = it.shops.filterNot { d -> d.shop.id == shop.id }) }
            favoriteRepository.removeFavorite(shop.id)
                .onSuccess { FavoriteSync.notifyChanged(shop.id, false) }
                .onFailure { load(force = true) }
        }
    }
}
