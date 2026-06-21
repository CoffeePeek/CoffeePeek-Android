package com.coffeepeek.admin.ui.screen.checkins

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.CheckIn
import com.coffeepeek.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VisitedPlacesUiState(
    val checkIns: List<CheckIn> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class VisitedPlacesViewModel(
    private val checkInRepository: CheckInRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(VisitedPlacesUiState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        workScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            checkInRepository.getMyCheckIns(page = 1, pageSize = 50)
                .onSuccess { page -> _state.update { it.copy(checkIns = page.items, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
