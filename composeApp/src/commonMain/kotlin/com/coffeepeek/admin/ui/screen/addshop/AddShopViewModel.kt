package com.coffeepeek.admin.ui.screen.addshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.City
import com.coffeepeek.domain.model.CreateShopInput
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AddShopStep { BASIC, CONTACTS, FEATURES }

data class AddShopUiState(
    // Каталоги
    val cities: List<City> = emptyList(),
    val beans: List<CatalogItem> = emptyList(),
    val equipment: List<CatalogItem> = emptyList(),
    val roasters: List<CatalogItem> = emptyList(),
    val brewMethods: List<CatalogItem> = emptyList(),

    // Форма — шаг 1
    val name: String = "",
    val selectedCity: City? = null,
    val address: String = "",
    val description: String = "",
    val priceRange: Int? = null,

    // Форма — шаг 2
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val instagram: String = "",

    // Форма — шаг 3
    val selectedBeanIds: Set<String> = emptySet(),
    val selectedEquipmentIds: Set<String> = emptySet(),
    val selectedRoasterIds: Set<String> = emptySet(),
    val selectedBrewMethodIds: Set<String> = emptySet(),

    // Состояние
    val currentStep: AddShopStep = AddShopStep.BASIC,
    val isLoadingCatalogs: Boolean = true,
    val isSubmitting: Boolean = false,
    val catalogsError: String? = null,
    val submitError: String? = null,
    val isSuccess: Boolean = false,
) {
    val nameError: String? get() = when {
        name.isBlank() -> "Введите название"
        name.length > 55 -> "Не более 55 символов"
        else -> null
    }
    val addressError: String? get() = if (address.isBlank()) "Введите адрес" else null
    val cityError: String? get() = if (selectedCity == null) "Выберите город" else null

    val step1Valid get() = nameError == null && addressError == null && cityError == null
}

class AddShopViewModel(
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddShopUiState())
    val state: StateFlow<AddShopUiState> = _state.asStateFlow()

    init { loadCatalogs() }

    private fun loadCatalogs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingCatalogs = true, catalogsError = null) }
            shopRepository.getCatalogs()
                .onSuccess { catalogs ->
                    _state.update { it.copy(
                        cities       = catalogs.cities,
                        beans        = catalogs.beans,
                        equipment    = catalogs.equipment,
                        roasters     = catalogs.roasters,
                        brewMethods  = catalogs.brewMethods,
                        isLoadingCatalogs = false,
                    ) }
                }
                .onFailure { err ->
                    _state.update { it.copy(
                        isLoadingCatalogs = false,
                        catalogsError = err.message ?: "Ошибка загрузки каталогов",
                    ) }
                }
        }
    }

    // ── Поля шага 1 ───────────────────────────────────────────────────────────
    fun onNameChange(v: String)         { _state.update { it.copy(name = v.take(55)) } }
    fun onAddressChange(v: String)      { _state.update { it.copy(address = v) } }
    fun onDescriptionChange(v: String)  { _state.update { it.copy(description = v) } }
    fun onCitySelect(city: City)        { _state.update { it.copy(selectedCity = city) } }
    fun onPriceRangeSelect(v: Int?)     { _state.update { it.copy(priceRange = v) } }

    // ── Поля шага 2 ───────────────────────────────────────────────────────────
    fun onPhoneChange(v: String)        { _state.update { it.copy(phone = v) } }
    fun onEmailChange(v: String)        { _state.update { it.copy(email = v) } }
    fun onWebsiteChange(v: String)      { _state.update { it.copy(website = v) } }
    fun onInstagramChange(v: String)    { _state.update { it.copy(instagram = v) } }

    // ── Шаг 3: мультивыбор ────────────────────────────────────────────────────
    fun toggleBean(id: String) {
        _state.update { s ->
            val c = s.selectedBeanIds
            s.copy(selectedBeanIds = if (id in c) c - id else c + id)
        }
    }

    fun toggleEquipment(id: String) {
        _state.update { s ->
            val c = s.selectedEquipmentIds
            s.copy(selectedEquipmentIds = if (id in c) c - id else c + id)
        }
    }

    fun toggleRoaster(id: String) {
        _state.update { s ->
            val c = s.selectedRoasterIds
            s.copy(selectedRoasterIds = if (id in c) c - id else c + id)
        }
    }

    fun toggleBrewMethod(id: String) {
        _state.update { s ->
            val c = s.selectedBrewMethodIds
            s.copy(selectedBrewMethodIds = if (id in c) c - id else c + id)
        }
    }

    // ── Навигация по шагам ────────────────────────────────────────────────────
    fun nextStep() {
        val s = _state.value
        when (s.currentStep) {
            AddShopStep.BASIC     -> if (s.step1Valid) _state.update { it.copy(currentStep = AddShopStep.CONTACTS) }
            AddShopStep.CONTACTS  -> _state.update { it.copy(currentStep = AddShopStep.FEATURES) }
            AddShopStep.FEATURES  -> submit()
        }
    }

    fun prevStep() {
        _state.update {
            it.copy(currentStep = when (it.currentStep) {
                AddShopStep.CONTACTS -> AddShopStep.BASIC
                AddShopStep.FEATURES -> AddShopStep.CONTACTS
                else -> it.currentStep
            })
        }
    }

    fun clearSubmitError() { _state.update { it.copy(submitError = null) } }

    // ── Отправка ──────────────────────────────────────────────────────────────
    private fun submit() {
        val s = _state.value
        val city = s.selectedCity ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, submitError = null) }
            shopRepository.createShop(
                CreateShopInput(
                    name        = s.name.trim(),
                    address     = s.address.trim(),
                    cityId      = city.id,
                    description = s.description.trim().takeIf { it.isNotEmpty() },
                    priceRange  = s.priceRange,
                    phone       = s.phone.trim().takeIf { it.isNotEmpty() },
                    email       = s.email.trim().takeIf { it.isNotEmpty() },
                    website     = s.website.trim().takeIf { it.isNotEmpty() },
                    instagram   = s.instagram.trim().takeIf { it.isNotEmpty() },
                    equipmentIds  = s.selectedEquipmentIds.toList(),
                    coffeeBeanIds = s.selectedBeanIds.toList(),
                    roasterIds    = s.selectedRoasterIds.toList(),
                    brewMethodIds = s.selectedBrewMethodIds.toList(),
                )
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false, isSuccess = true) }
            }.onFailure { err ->
                _state.update { it.copy(
                    isSubmitting = false,
                    submitError = err.message ?: "Ошибка отправки",
                ) }
            }
        }
    }

    fun onSuccessDismiss() {
        Navigator.popBack()
    }
}
