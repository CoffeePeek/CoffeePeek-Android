package com.coffeepeek.admin.ui.screen.feed

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.CoffeeShop
import com.coffeepeek.domain.model.ShopFilters
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val shops: List<CoffeeShop> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false,
)

class FeedViewModel(
    private val shopRepository: ShopRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadShops(reset = true)
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
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

            shopRepository.searchShops(
                ShopFilters(
                    query = state.query.takeIf { it.isNotBlank() },
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
}
