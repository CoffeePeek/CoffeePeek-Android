package com.coffeepeek.admin.ui.screen.reviews

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.Review
import com.coffeepeek.domain.repository.ReviewRepository
import com.coffeepeek.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyReviewsUiState(
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class MyReviewsViewModel(
    private val reviewRepository: ReviewRepository,
    private val sessionRepository: SessionRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(MyReviewsUiState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        workScope.launch {
            val userId = sessionRepository.getSession()?.userId
            if (userId.isNullOrBlank()) {
                _state.update { it.copy(error = "Пользователь не авторизован", isLoading = false) }
                return@launch
            }
            _state.update { it.copy(isLoading = true, error = null) }
            reviewRepository.getUserReviews(userId, page = 1, pageSize = 50)
                .onSuccess { page -> _state.update { it.copy(reviews = page.items, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
