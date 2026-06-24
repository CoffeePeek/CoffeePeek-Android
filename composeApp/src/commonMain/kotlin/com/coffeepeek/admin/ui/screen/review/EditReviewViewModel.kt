package com.coffeepeek.admin.ui.screen.review

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.MAX_REVIEW_PHOTOS
import com.coffeepeek.admin.utils.PickedImage
import com.coffeepeek.domain.model.PendingPhotoUpload
import com.coffeepeek.domain.model.UpdateReviewInput
import com.coffeepeek.domain.repository.ReviewRepository
import com.coffeepeek.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditReviewUiState(
    val isLoading: Boolean = true,
    val header: String = "",
    val comment: String = "",
    val placeRating: Int = 5,
    val serviceRating: Int = 5,
    val coffeeRating: Int = 5,
    val existingPhotoUrls: List<String> = emptyList(),
    val newPhotos: List<PickedImage> = emptyList(),
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

class EditReviewViewModel(
    private val reviewId: String,
    private val reviewRepository: ReviewRepository,
    private val sessionRepository: SessionRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(EditReviewUiState())
    val state = _state.asStateFlow()

    init {
        loadReview()
    }

    fun loadReview() {
        workScope.launch {
            val userId = sessionRepository.getSession()?.userId
            if (userId.isNullOrBlank()) {
                _state.update { it.copy(isLoading = false, error = "Пользователь не авторизован") }
                return@launch
            }
            _state.update { it.copy(isLoading = true, error = null) }
            reviewRepository.getUserReviews(userId, page = 1, pageSize = 100)
                .onSuccess { page ->
                    val review = page.items.find { it.id == reviewId }
                    if (review == null) {
                        _state.update { it.copy(isLoading = false, error = "Отзыв не найден") }
                        return@onSuccess
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            header = review.header,
                            comment = review.comment,
                            placeRating = review.rating.place.coerceIn(1, 5),
                            serviceRating = review.rating.service.coerceIn(1, 5),
                            coffeeRating = review.rating.coffee.coerceIn(1, 5),
                            existingPhotoUrls = review.photoUrls,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onHeaderChange(v: String) { _state.update { it.copy(header = v.take(120)) } }
    fun onCommentChange(v: String) { _state.update { it.copy(comment = v.take(2000)) } }
    fun onPlaceRating(v: Int) { _state.update { it.copy(placeRating = v.coerceIn(1, 5)) } }
    fun onServiceRating(v: Int) { _state.update { it.copy(serviceRating = v.coerceIn(1, 5)) } }
    fun onCoffeeRating(v: Int) { _state.update { it.copy(coffeeRating = v.coerceIn(1, 5)) } }

    fun addPhotos(images: List<PickedImage>) {
        if (images.isEmpty()) return
        _state.update { state ->
            val remaining = MAX_REVIEW_PHOTOS - state.newPhotos.size
            if (remaining <= 0) return@update state
            state.copy(newPhotos = state.newPhotos + images.take(remaining))
        }
    }

    fun removeNewPhoto(index: Int) {
        _state.update { state ->
            state.copy(newPhotos = state.newPhotos.filterIndexed { i, _ -> i != index })
        }
    }

    fun submit() {
        val s = _state.value
        if (s.header.isBlank() || s.comment.isBlank()) {
            _state.update { it.copy(error = "Заполните заголовок и текст отзыва") }
            return
        }
        workScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            reviewRepository.updateReview(
                reviewId = reviewId,
                input = UpdateReviewInput(
                    header = s.header.trim(),
                    comment = s.comment.trim(),
                    placeRating = s.placeRating,
                    serviceRating = s.serviceRating,
                    coffeeRating = s.coffeeRating,
                    photos = s.newPhotos.map { it.toPendingUpload() },
                )
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false) }
                Navigator.popBack()
            }.onFailure { e ->
                _state.update { it.copy(isSubmitting = false, error = e.message) }
            }
        }
    }
}

private fun PickedImage.toPendingUpload() = PendingPhotoUpload(
    fileName = fileName,
    contentType = contentType,
    bytes = bytes,
)
