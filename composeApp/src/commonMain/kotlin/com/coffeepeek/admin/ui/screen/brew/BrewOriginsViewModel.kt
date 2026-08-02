package com.coffeepeek.admin.ui.screen.brew

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.BrewSessionDetails
import com.coffeepeek.domain.model.OriginStat
import com.coffeepeek.domain.repository.BrewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BrewOriginsUiState(
    val origins: List<OriginStat> = emptyList(),
    val selectedCountry: String? = null,
    val sessions: List<BrewSessionDetails> = emptyList(),
    val isLoading: Boolean = true,
)

class BrewOriginsViewModel(
    private val brewRepository: BrewRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(BrewOriginsUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        workScope.launch {
            _state.update { it.copy(isLoading = true) }
            val origins = brewRepository.getOriginStats()
            _state.update { it.copy(origins = origins, isLoading = false) }
        }
    }

    fun selectCountry(code: String) {
        workScope.launch {
            val sessions = brewRepository.getSessionsByOrigin(code)
            _state.update { it.copy(selectedCountry = code, sessions = sessions) }
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedCountry = null, sessions = emptyList()) }
    }
}
