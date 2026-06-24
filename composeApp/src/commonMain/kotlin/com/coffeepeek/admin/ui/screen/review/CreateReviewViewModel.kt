package com.coffeepeek.admin.ui.screen.review

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.MAX_REVIEW_PHOTOS
import com.coffeepeek.admin.utils.PickedImage
import com.coffeepeek.domain.model.CreateReviewInput
import com.coffeepeek.domain.model.PendingPhotoUpload
import com.coffeepeek.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateReviewUiState(
    val header: String = "",
    val comment: String = "",
    val placeRating: Int = 5,
    val serviceRating: Int = 5,
    val coffeeRating: Int = 5,
    val photos: List<PickedImage> = emptyList(),
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

class CreateReviewViewModel(
    private val shopId: String,
    private val reviewRepository: ReviewRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(CreateReviewUiState())
    val state = _state.asStateFlow()

    fun onHeaderChange(v: String) { _state.update { it.copy(header = v.take(120)) } }
    fun onCommentChange(v: String) { _state.update { it.copy(comment = v.take(2000)) } }
    fun onPlaceRating(v: Int) { _state.update { it.copy(placeRating = v.coerceIn(1, 5)) } }
    fun onServiceRating(v: Int) { _state.update { it.copy(serviceRating = v.coerceIn(1, 5)) } }
    fun onCoffeeRating(v: Int) { _state.update { it.copy(coffeeRating = v.coerceIn(1, 5)) } }

    fun addPhotos(images: List<PickedImage>) {
        if (images.isEmpty()) return
        _state.update { state ->
            val remaining = MAX_REVIEW_PHOTOS - state.photos.size
            if (remaining <= 0) return@update state
            state.copy(photos = state.photos + images.take(remaining))
        }
    }

    fun removePhoto(index: Int) {
        _state.update { state ->
            state.copy(photos = state.photos.filterIndexed { i, _ -> i != index })
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
            reviewRepository.createReview(
                CreateReviewInput(
                    shopId = shopId,
                    header = s.header.trim(),
                    comment = s.comment.trim(),
                    placeRating = s.placeRating,
                    serviceRating = s.serviceRating,
                    coffeeRating = s.coffeeRating,
                    photos = s.photos.map { it.toPendingUpload() },
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
