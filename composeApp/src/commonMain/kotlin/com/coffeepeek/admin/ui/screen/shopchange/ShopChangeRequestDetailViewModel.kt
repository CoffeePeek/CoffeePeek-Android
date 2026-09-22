package com.coffeepeek.admin.ui.screen.shopchange

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.model.ModerationStatus
import com.coffeepeek.domain.model.ShopChangeRequest
import com.coffeepeek.domain.repository.ShopChangeRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopChangeRequestDetailUiState(
    val request: ShopChangeRequest? = null,
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val error: String? = null,
    val rejectComment: String = "",
    val showRejectDialog: Boolean = false,
)

class ShopChangeRequestDetailViewModel(
    private val requestId: String,
    private val isModerator: Boolean,
    private val repository: ShopChangeRequestRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(ShopChangeRequestDetailUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        workScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getById(requestId)
                .onSuccess { request -> _state.update { it.copy(request = request, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun openEditor() {
        val request = _state.value.request ?: return
        Navigator.navigate(
            Navigator.Screen.ShopChangeEditor(
                shopId = request.shopId,
                section = request.section.name,
                requestId = request.id,
            ),
        )
    }

    fun approve() = decide(ModerationStatus.Approved, comment = null)

    fun onRejectCommentChange(value: String) = _state.update { it.copy(rejectComment = value.take(500)) }

    fun showRejectDialog() = _state.update { it.copy(showRejectDialog = true, rejectComment = "") }

    fun hideRejectDialog() = _state.update { it.copy(showRejectDialog = false) }

    fun reject() {
        val comment = _state.value.rejectComment.trim()
        if (comment.isBlank()) {
            _state.update { it.copy(error = "Укажите причину отклонения") }
            return
        }
        decide(ModerationStatus.Rejected, comment)
    }

    fun clearError() = _state.update { it.copy(error = null) }

    private fun decide(status: ModerationStatus, comment: String?) {
        workScope.launch {
            _state.update { it.copy(isWorking = true, error = null, showRejectDialog = false) }
            repository.decide(requestId, status, comment)
                .onSuccess {
                    _state.update { it.copy(isWorking = false) }
                    Navigator.popBack()
                }
                .onFailure { e ->
                    _state.update { it.copy(isWorking = false, error = e.message) }
                }
        }
    }
}
