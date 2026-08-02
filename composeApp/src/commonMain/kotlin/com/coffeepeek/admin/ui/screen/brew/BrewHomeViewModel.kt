package com.coffeepeek.admin.ui.screen.brew

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.BrewSessionDetails
import com.coffeepeek.domain.model.BrewTrends
import com.coffeepeek.domain.repository.BrewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BrewHomeUiState(
    val recent: List<BrewSessionDetails> = emptyList(),
    val beanCount: Int = 0,
    val weekTrends: BrewTrends? = null,
    val isLoading: Boolean = true,
)

class BrewHomeViewModel(
    private val brewRepository: BrewRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(BrewHomeUiState())
    val state = _state.asStateFlow()

    init {
        brewRepository.observeSessions()
            .onEach { sessions ->
                _state.update {
                    it.copy(
                        recent = sessions.take(5),
                        isLoading = false,
                        weekTrends = brewRepository.getTrends(7),
                    )
                }
            }
            .launchIn(workScope)

        brewRepository.observeBeans()
            .onEach { beans -> _state.update { it.copy(beanCount = beans.size) } }
            .launchIn(workScope)
    }
}
