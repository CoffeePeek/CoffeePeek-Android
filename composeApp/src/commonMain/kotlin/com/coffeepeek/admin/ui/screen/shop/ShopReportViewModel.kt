package com.coffeepeek.admin.ui.screen.shop

import com.coffeepeek.admin.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ShopReportReason(val title: String, val apiValue: String) {
    OUTDATED_MENU("Меню больше не актуально", "outdated_menu"),
    SHOP_CLOSED("Кофейня закрылась", "shop_closed"),
    INCORRECT_SCHEDULE("В расписании есть ошибка", "incorrect_schedule"),
    INCORRECT_PHOTOS("Фотографии не соответствуют кофейне", "incorrect_photos"),
    INCORRECT_ADDRESS("Указан неверный адрес", "incorrect_address"),
    OTHER("Другая проблема", "other"),
}

data class ShopReportUiState(
    val selectedReason: ShopReportReason? = null,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
)

class ShopReportViewModel(
    private val shopId: String,
) : BaseViewModel() {

    private val _state = MutableStateFlow(ShopReportUiState())
    val state = _state.asStateFlow()

    fun selectReason(reason: ShopReportReason) {
        _state.update { it.copy(selectedReason = reason, error = null) }
    }

    fun updateComment(comment: String) {
        _state.update { it.copy(comment = comment.take(MAX_COMMENT_LENGTH), error = null) }
    }

    fun submit() {
        val current = _state.value
        if (current.selectedReason == null) {
            _state.update { it.copy(error = "Выберите, какие данные нужно исправить") }
            return
        }
        if (current.selectedReason == ShopReportReason.OTHER && current.comment.isBlank()) {
            _state.update { it.copy(error = "Опишите проблему") }
            return
        }

        workScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            // В опубликованном API пока нет эндпоинта для таких обращений.
            // Мок оставлен здесь, чтобы сетевой вызов можно было заменить в одном месте.
            submitMock(
                shopId = shopId,
                reason = current.selectedReason.apiValue,
                comment = current.comment.trim().takeIf(String::isNotEmpty),
            )

            _state.update { it.copy(isSubmitting = false, isSubmitted = true) }
        }
    }

    private suspend fun submitMock(shopId: String, reason: String, comment: String?) {
        @Suppress("UNUSED_VARIABLE")
        val request = Triple(shopId, reason, comment)
        delay(500)
    }

    private companion object {
        const val MAX_COMMENT_LENGTH = 500
    }
}
