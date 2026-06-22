package com.coffeepeek.admin.ui.screen.checkins

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.CheckIn
import com.coffeepeek.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

data class VisitedPlacesUiState(
    val checkIns: List<CheckIn> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
)

class VisitedPlacesViewModel(
    private val checkInRepository: CheckInRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(VisitedPlacesUiState())
    val state = _state.asStateFlow()

    init { load(reset = true) }

    fun load(reset: Boolean = false) {
        workScope.launch {
            val page = if (reset) 1 else _state.value.currentPage + 1
            if (!reset && (!_state.value.hasMore || _state.value.isLoadingMore)) return@launch

            _state.update {
                it.copy(
                    isLoading = reset,
                    isLoadingMore = !reset,
                    error = null,
                )
            }

            checkInRepository.getMyCheckIns(page = page, pageSize = PAGE_SIZE)
                .onSuccess { result ->
                    _state.update { state ->
                        state.copy(
                            checkIns = if (reset) result.items else state.checkIns + result.items,
                            isLoading = false,
                            isLoadingMore = false,
                            currentPage = result.currentPage,
                            hasMore = result.currentPage < result.totalPages,
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
