package com.coffeepeek.admin.ui.screen.brew

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.BeanBag
import com.coffeepeek.domain.repository.BrewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BeanListUiState(
    val beans: List<BeanBag> = emptyList(),
    val isLoading: Boolean = true,
)

class BeanListViewModel(
    private val brewRepository: BrewRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(BeanListUiState())
    val state = _state.asStateFlow()

    init {
        brewRepository.observeBeans()
            .onEach { beans -> _state.update { it.copy(beans = beans, isLoading = false) } }
            .launchIn(workScope)
    }

    fun delete(id: String) {
        workScope.launch { brewRepository.deleteBean(id) }
    }
}
