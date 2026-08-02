package com.coffeepeek.admin.ui.screen.brew

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.brew.CoffeeOriginCountries
import com.coffeepeek.domain.model.NewBeanBagInput
import com.coffeepeek.domain.model.RoastLevel
import com.coffeepeek.domain.repository.BrewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BeanEditUiState(
    val name: String = "",
    val originCountryCode: String = "ET",
    val roastLevel: RoastLevel = RoastLevel.MEDIUM,
    val roasterName: String = "",
    val notes: String = "",
    val countries: List<CoffeeOriginCountries.Country> = CoffeeOriginCountries.all,
    val isSaving: Boolean = false,
    val error: String? = null,
)

class BeanEditViewModel(
    private val beanId: String,
    private val brewRepository: BrewRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(BeanEditUiState())
    val state = _state.asStateFlow()

    init {
        if (beanId.isNotBlank()) {
            workScope.launch {
                brewRepository.getBean(beanId)?.let { bean ->
                    _state.update {
                        it.copy(
                            name = bean.name,
                            originCountryCode = bean.originCountryCode,
                            roastLevel = bean.roastLevel,
                            roasterName = bean.roasterName,
                            notes = bean.notes,
                        )
                    }
                }
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v) }
    fun onRoaster(v: String) = _state.update { it.copy(roasterName = v) }
    fun onNotes(v: String) = _state.update { it.copy(notes = v) }
    fun onOrigin(code: String) = _state.update { it.copy(originCountryCode = code) }
    fun onRoast(level: RoastLevel) = _state.update { it.copy(roastLevel = level) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "Укажите название зерна") }
            return
        }
        workScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            runCatching {
                brewRepository.upsertBean(
                    NewBeanBagInput(
                        name = s.name,
                        originCountryCode = s.originCountryCode,
                        roastLevel = s.roastLevel,
                        roasterName = s.roasterName,
                        notes = s.notes,
                    ),
                    id = beanId.takeIf { it.isNotBlank() },
                )
            }.onSuccess {
                _state.update { it.copy(isSaving = false) }
                Navigator.popBack()
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
