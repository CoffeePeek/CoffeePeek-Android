package com.coffeepeek.admin.ui.screen.brew

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.BrewTrends
import com.coffeepeek.domain.repository.BrewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BrewTrendsUiState(
    val periodDays: Int = 7,
    val trends: BrewTrends? = null,
    val isLoading: Boolean = true,
)

class BrewTrendsViewModel(
    private val brewRepository: BrewRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(BrewTrendsUiState())
    val state = _state.asStateFlow()

    init {
        load(7)
    }

    fun load(periodDays: Int) {
        workScope.launch {
            _state.update { it.copy(isLoading = true, periodDays = periodDays) }
            val trends = brewRepository.getTrends(periodDays)
            _state.update { it.copy(trends = trends, isLoading = false) }
        }
    }
}
