package com.coffeepeek.admin.ui.screen.shopchange

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.ShopChangeRequest
import com.coffeepeek.domain.repository.ShopChangeRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

data class ShopChangeRequestsUiState(
    val items: List<ShopChangeRequest> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
)

class ShopChangeRequestsViewModel(
    private val repository: ShopChangeRequestRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(ShopChangeRequestsUiState())
    val state = _state.asStateFlow()

    init {
        load(reset = true)
    }

    fun load(reset: Boolean = false) {
        workScope.launch {
            val page = if (reset) 1 else _state.value.currentPage + 1
            if (!reset && (!_state.value.hasMore || _state.value.isLoadingMore)) return@launch
            _state.update {
                it.copy(isLoading = reset, isLoadingMore = !reset, error = null)
            }
            val result = repository.getMine(page, PAGE_SIZE)
            result
                .onSuccess { pageResult ->
                    _state.update { state ->
                        state.copy(
                            items = if (reset) pageResult.items else state.items + pageResult.items,
                            isLoading = false,
                            isLoadingMore = false,
                            currentPage = pageResult.currentPage,
                            hasMore = pageResult.currentPage < pageResult.totalPages,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, isLoadingMore = false, error = e.message) }
                }
        }
    }

    fun loadMore() = load(reset = false)
    fun refresh() = load(reset = true)
}
