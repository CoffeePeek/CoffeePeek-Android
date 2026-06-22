package com.coffeepeek.admin.ui.screen.reviews

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.Review
import com.coffeepeek.domain.repository.ReviewRepository
import com.coffeepeek.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

data class MyReviewsUiState(
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
)

class MyReviewsViewModel(
    private val reviewRepository: ReviewRepository,
    private val sessionRepository: SessionRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(MyReviewsUiState())
    val state = _state.asStateFlow()

    init { load(reset = true) }

    fun load(reset: Boolean = false) {
        workScope.launch {
            val userId = sessionRepository.getSession()?.userId
            if (userId.isNullOrBlank()) {
                _state.update { it.copy(error = "Пользователь не авторизован", isLoading = false) }
                return@launch
            }

            val page = if (reset) 1 else _state.value.currentPage + 1
            if (!reset && (!_state.value.hasMore || _state.value.isLoadingMore)) return@launch

            _state.update {
                it.copy(
                    isLoading = reset,
                    isLoadingMore = !reset,
                    error = null,
                )
            }

            reviewRepository.getUserReviews(userId, page = page, pageSize = PAGE_SIZE)
                .onSuccess { result ->
                    _state.update { state ->
                        state.copy(
                            reviews = if (reset) result.items else state.reviews + result.items,
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
