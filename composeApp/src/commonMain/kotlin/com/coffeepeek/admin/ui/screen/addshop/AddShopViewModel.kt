package com.coffeepeek.admin.ui.screen.addshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.City
import com.coffeepeek.domain.model.CreateShopInput
import com.coffeepeek.domain.model.PendingPhotoUpload
import com.coffeepeek.admin.utils.MAX_SHOP_PHOTOS
import com.coffeepeek.admin.utils.PickedImage
import com.coffeepeek.domain.model.ScheduleInterval
import com.coffeepeek.domain.model.ShopCatalogs
import com.coffeepeek.domain.model.ShopSchedule
import com.coffeepeek.admin.utils.validateOptionalEmail
import com.coffeepeek.admin.utils.validateOptionalInstagram
import com.coffeepeek.admin.utils.validateOptionalPhone
import com.coffeepeek.admin.utils.validateOptionalUrl
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AddShopStep { BASIC, PHOTOS, SCHEDULE, CONTACTS, FEATURES }

data class DayScheduleUi(
    val dayOfWeek: Int,
    val label: String,
    val shortLabel: String,
    val isClosed: Boolean = false,
    val openTime: String = "09:00",
    val closeTime: String = "21:00",
)

data class PendingPhotoUi(
    val fileName: String,
    val contentType: String,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PendingPhotoUi) return false
        return fileName == other.fileName && contentType == other.contentType && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

data class AddShopUiState(
    val cities: List<City> = emptyList(),
    val beans: List<CatalogItem> = emptyList(),
    val equipment: List<CatalogItem> = emptyList(),
    val roasters: List<CatalogItem> = emptyList(),
    val brewMethods: List<CatalogItem> = emptyList(),
    val name: String = "",
    val selectedCity: City? = null,
    val address: String = "",
    val description: String = "",
    val priceRange: Int? = null,
    val photos: List<PendingPhotoUi> = emptyList(),
    val unifiedOpenTime: String = "09:00",
    val unifiedCloseTime: String = "21:00",
    val closedDays: Set<Int> = emptySet(),
    val usePerDaySchedule: Boolean = false,
    val schedules: List<DayScheduleUi> = defaultSchedules(),
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val instagram: String = "",
    val selectedBeanIds: Set<String> = emptySet(),
    val selectedEquipmentIds: Set<String> = emptySet(),
    val selectedRoasterIds: Set<String> = emptySet(),
    val selectedBrewMethodIds: Set<String> = emptySet(),
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
    val phoneError: String? get() = validateOptionalPhone(phone)
    val emailError: String? get() = validateOptionalEmail(email)
    val websiteError: String? get() = validateOptionalUrl(website)
    val instagramError: String? get() = validateOptionalInstagram(instagram)
    val step1Valid get() = nameError == null && addressError == null && cityError == null
    val contactsStepValid get() = phoneError == null && emailError == null &&
        websiteError == null && instagramError == null
}

private val weekDays = listOf(
    1 to ("Понедельник" to "Пн"),
    2 to ("Вторник" to "Вт"),
    3 to ("Среда" to "Ср"),
    4 to ("Четверг" to "Чт"),
    5 to ("Пятница" to "Пт"),
    6 to ("Суббота" to "Сб"),
    0 to ("Воскресенье" to "Вс"),
)

private fun defaultSchedules() = weekDays.map { (day, labels) ->
    DayScheduleUi(
        dayOfWeek = day,
        label = labels.first,
        shortLabel = labels.second,
        isClosed = false,
        openTime = "09:00",
        closeTime = "21:00",
    )
}

internal fun shiftTime(time: String, minutesDelta: Int): String {
    val parts = time.split(":")
    val totalMinutes = (parts.getOrNull(0)?.toIntOrNull() ?: 9) * 60 +
        (parts.getOrNull(1)?.toIntOrNull() ?: 0) + minutesDelta
    val clamped = totalMinutes.coerceIn(0, 23 * 60 + 59)
    return "%02d:%02d".format(clamped / 60, clamped % 60)
}

class AddShopViewModel(
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddShopUiState())
    val state: StateFlow<AddShopUiState> = _state.asStateFlow()

    init { loadCatalogs() }

    fun loadCatalogs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingCatalogs = true, catalogsError = null) }
            shopRepository.getCatalogs()
                .onSuccess { catalogs -> applyCatalogs(catalogs) }
                .onFailure { err ->
                    _state.update { it.copy(
                        isLoadingCatalogs = false,
                        catalogsError = err.message ?: "Ошибка загрузки каталогов",
                    ) }
                }
        }
    }

    private fun applyCatalogs(catalogs: ShopCatalogs) {
        val autoCity = catalogs.cities.singleOrNull()
        _state.update { it.copy(
            cities       = catalogs.cities,
            beans        = catalogs.beans,
            equipment    = catalogs.equipment,
            roasters     = catalogs.roasters,
            brewMethods  = catalogs.brewMethods,
            selectedCity = it.selectedCity ?: autoCity,
            isLoadingCatalogs = false,
        ) }
    }

    fun onNameChange(v: String)         { _state.update { it.copy(name = v.take(55)) } }
    fun onAddressChange(v: String)      { _state.update { it.copy(address = v) } }
    fun onDescriptionChange(v: String)  { _state.update { it.copy(description = v) } }
    fun onCitySelect(city: City)        { _state.update { it.copy(selectedCity = city) } }
    fun onPriceRangeSelect(v: Int?)     { _state.update { it.copy(priceRange = v) } }
    fun onPhoneChange(v: String)        { _state.update { it.copy(phone = v) } }
    fun onEmailChange(v: String)        { _state.update { it.copy(email = v) } }
    fun onWebsiteChange(v: String)      { _state.update { it.copy(website = v) } }
    fun onInstagramChange(v: String)    { _state.update { it.copy(instagram = v) } }

    fun addPhoto(bytes: ByteArray, fileName: String = "photo_${kotlin.random.Random.nextInt()}.jpg") {
        addPhotos(listOf(PickedImage(bytes, fileName)))
    }

    fun addPhotos(images: List<PickedImage>) {
        if (images.isEmpty()) return
        _state.update { s ->
            val remaining = MAX_SHOP_PHOTOS - s.photos.size
            if (remaining <= 0) return@update s
            val toAdd = images.take(remaining).map { image ->
                PendingPhotoUi(image.fileName, image.contentType, image.bytes)
            }
            s.copy(photos = s.photos + toAdd)
        }
    }

    fun removePhoto(index: Int) {
        _state.update { s -> s.copy(photos = s.photos.filterIndexed { i, _ -> i != index }) }
    }

    fun updateSchedule(dayOfWeek: Int, transform: (DayScheduleUi) -> DayScheduleUi) {
        _state.update { s ->
            s.copy(schedules = s.schedules.map { if (it.dayOfWeek == dayOfWeek) transform(it) else it })
        }
    }

    fun toggleClosedDay(dayOfWeek: Int) {
        _state.update { s ->
            val closed = if (dayOfWeek in s.closedDays) s.closedDays - dayOfWeek else s.closedDays + dayOfWeek
            s.copy(closedDays = closed)
        }
    }

    fun adjustUnifiedOpen(minutesDelta: Int) {
        _state.update { it.copy(unifiedOpenTime = shiftTime(it.unifiedOpenTime, minutesDelta)) }
    }

    fun adjustUnifiedClose(minutesDelta: Int) {
        _state.update { it.copy(unifiedCloseTime = shiftTime(it.unifiedCloseTime, minutesDelta)) }
    }

    fun applySchedulePreset(openTime: String, closeTime: String) {
        _state.update { it.copy(unifiedOpenTime = openTime, unifiedCloseTime = closeTime) }
    }

    fun setUsePerDaySchedule(enabled: Boolean) {
        _state.update { s ->
            if (!enabled) return@update s.copy(usePerDaySchedule = false)
            val synced = weekDays.map { (day, labels) ->
                DayScheduleUi(
                    dayOfWeek = day,
                    label = labels.first,
                    shortLabel = labels.second,
                    isClosed = day in s.closedDays,
                    openTime = s.unifiedOpenTime,
                    closeTime = s.unifiedCloseTime,
                )
            }
            s.copy(usePerDaySchedule = true, schedules = synced)
        }
    }

    fun adjustDayOpenTime(dayOfWeek: Int, minutesDelta: Int) {
        updateSchedule(dayOfWeek) { it.copy(openTime = shiftTime(it.openTime, minutesDelta)) }
    }

    fun adjustDayCloseTime(dayOfWeek: Int, minutesDelta: Int) {
        updateSchedule(dayOfWeek) { it.copy(closeTime = shiftTime(it.closeTime, minutesDelta)) }
    }

    private fun buildSchedulesForSubmit(state: AddShopUiState): List<ShopSchedule> {
        return if (state.usePerDaySchedule) {
            state.schedules.map { day ->
                ShopSchedule(
                    dayOfWeek = day.dayOfWeek,
                    isClosed = day.isClosed,
                    intervals = if (day.isClosed) emptyList() else {
                        listOf(ScheduleInterval(day.openTime, day.closeTime))
                    },
                )
            }
        } else {
            weekDays.map { (day, _) ->
                ShopSchedule(
                    dayOfWeek = day,
                    isClosed = day in state.closedDays,
                    intervals = if (day in state.closedDays) {
                        emptyList()
                    } else {
                        listOf(ScheduleInterval(state.unifiedOpenTime, state.unifiedCloseTime))
                    },
                )
            }
        }
    }

    fun toggleBean(id: String) = toggleSet(id, AddShopUiState::selectedBeanIds) { copy(selectedBeanIds = it) }
    fun toggleEquipment(id: String) = toggleSet(id, AddShopUiState::selectedEquipmentIds) { copy(selectedEquipmentIds = it) }
    fun toggleRoaster(id: String) = toggleSet(id, AddShopUiState::selectedRoasterIds) { copy(selectedRoasterIds = it) }
    fun toggleBrewMethod(id: String) = toggleSet(id, AddShopUiState::selectedBrewMethodIds) { copy(selectedBrewMethodIds = it) }

    private inline fun toggleSet(
        id: String,
        selector: (AddShopUiState) -> Set<String>,
        crossinline update: AddShopUiState.(Set<String>) -> AddShopUiState,
    ) {
        _state.update { s ->
            val current = selector(s)
            s.update(if (id in current) current - id else current + id)
        }
    }

    fun nextStep() {
        val s = _state.value
        when (s.currentStep) {
            AddShopStep.BASIC -> if (s.step1Valid) _state.update { it.copy(currentStep = AddShopStep.PHOTOS) }
            AddShopStep.PHOTOS -> _state.update { it.copy(currentStep = AddShopStep.SCHEDULE) }
            AddShopStep.SCHEDULE -> _state.update { it.copy(currentStep = AddShopStep.CONTACTS) }
            AddShopStep.CONTACTS -> if (s.contactsStepValid) {
                _state.update { it.copy(currentStep = AddShopStep.FEATURES) }
            }
            AddShopStep.FEATURES -> submit()
        }
    }

    fun prevStep() {
        _state.update {
            it.copy(currentStep = when (it.currentStep) {
                AddShopStep.PHOTOS -> AddShopStep.BASIC
                AddShopStep.SCHEDULE -> AddShopStep.PHOTOS
                AddShopStep.CONTACTS -> AddShopStep.SCHEDULE
                AddShopStep.FEATURES -> AddShopStep.CONTACTS
                else -> it.currentStep
            })
        }
    }

    fun clearSubmitError() { _state.update { it.copy(submitError = null) } }

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
                    photos = s.photos.map { PendingPhotoUpload(it.fileName, it.contentType, it.bytes) },
                    schedules = buildSchedulesForSubmit(s),
                )
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false, isSuccess = true) }
            }.onFailure { err ->
                _state.update { it.copy(
                    isSubmitting = false,
                    submitError = err.message
                        ?.substringBefore("Expected type")
                        ?.trim()
                        ?.ifBlank { null }
                        ?: "Ошибка отправки. Попробуйте выйти и войти снова.",
                ) }
            }
        }
    }

    fun onSuccessDismiss() {
        Navigator.popBack()
    }
}
