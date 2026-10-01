package com.coffeepeek.admin.ui.screen.shopchange

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.ShopChangeRequest
import com.coffeepeek.domain.repository.ShopChangeRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopChangeRequestDetailUiState(
    val request: ShopChangeRequest? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class ShopChangeRequestDetailViewModel(
    private val requestId: String,
    private val repository: ShopChangeRequestRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(ShopChangeRequestDetailUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        workScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getById(requestId)
                .onSuccess { request -> _state.update { it.copy(request = request, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) }             }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
