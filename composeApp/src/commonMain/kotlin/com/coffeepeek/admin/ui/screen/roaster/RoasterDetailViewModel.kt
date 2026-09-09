package com.coffeepeek.admin.ui.screen.roaster

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.RoasterDetails
import com.coffeepeek.domain.repository.RoasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoasterDetailUiState(
    val isLoading: Boolean = true,
    val details: RoasterDetails? = null,
    val error: String? = null,
)

class RoasterDetailViewModel(
    private val roasterId: String,
    private val repository: RoasterRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(RoasterDetailUiState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        workScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getRoaster(roasterId)
                .onSuccess { details ->
                    _state.update { it.copy(isLoading = false, details = details) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Не удалось загрузить обжарщика",
                        )
                    }
                }
        }
    }
}
