package com.coffeepeek.admin.ui.screen.shopchange

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SuggestShopChangeUiState(
    val shopTitle: String = "",
    val isLoading: Boolean = true,
)

class SuggestShopChangeViewModel(
    private val shopId: String,
    private val shopRepository: ShopRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(SuggestShopChangeUiState())
    val state = _state.asStateFlow()

    init {
        workScope.launch {
            shopRepository.getShopDetails(shopId)
                .onSuccess { details ->
                    _state.update {
                        it.copy(shopTitle = details.shop.title, isLoading = false)
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }
}
