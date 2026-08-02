package com.coffeepeek.admin.ui.screen.brew

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.model.BrewSessionDetails
import com.coffeepeek.domain.repository.BrewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BrewDetailUiState(
    val details: BrewSessionDetails? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class BrewDetailViewModel(
    private val sessionId: String,
    private val brewRepository: BrewRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(BrewDetailUiState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        workScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val details = brewRepository.getSession(sessionId)
            _state.update {
                it.copy(
                    details = details,
                    isLoading = false,
                    error = if (details == null) "Заварка не найдена" else null,
                )
            }
        }
    }

    fun repeat() {
        Navigator.navigate(Navigator.Screen.NewBrew(repeatSessionId = sessionId))
    }

    fun delete() {
        workScope.launch {
            brewRepository.deleteSession(sessionId)
            Navigator.popBack()
        }
    }
}
