package com.coffeepeek.admin.ui.screen.shop

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopDetailUiState(
    val details: CoffeeShopDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ShopDetailViewModel(
    private val shopId: String,
    private val shopRepository: ShopRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ShopDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        workScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            shopRepository.getShopDetails(shopId)
                .onSuccess { details ->
                    _uiState.update { it.copy(details = details, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
