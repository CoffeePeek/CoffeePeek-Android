package com.coffeepeek.admin.ui.screen.shop

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.FavoriteSync
import com.coffeepeek.admin.utils.ReviewSync
import com.coffeepeek.admin.utils.currentUtcIsoDateTime
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.CreateCheckInInput
import com.coffeepeek.domain.repository.CheckInRepository
import com.coffeepeek.domain.repository.FavoriteRepository
import com.coffeepeek.domain.repository.ReviewRepository
import com.coffeepeek.domain.repository.SessionRepository
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopDetailUiState(
    val details: CoffeeShopDetails? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val isFavoriteLoading: Boolean = false,
    val isCheckInLoading: Boolean = false,
    val showCheckInSheet: Boolean = false,
    val actionMessage: String? = null,
    val error: String? = null,
)

class ShopDetailViewModel(
    private val shopId: String,
    private val shopRepository: ShopRepository,
    private val favoriteRepository: FavoriteRepository,
    private val checkInRepository: CheckInRepository,
    private val reviewRepository: ReviewRepository,
    private val sessionRepository: SessionRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ShopDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
        ReviewSync.changes
            .onEach { changedShopId ->
                if (changedShopId == shopId) {
                    _uiState.update { it.copy(actionMessage = "Отзыв отправлен на модерацию") }
                    refreshDetails(showLoading = false)
                }
            }
            .launchIn(workScope)
    }

    fun load() {
        workScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            refreshDetails(showLoading = true)
        }
    }

    private suspend fun refreshDetails(showLoading: Boolean) {
        val isLoggedIn = sessionRepository.isLoggedIn()
        shopRepository.getShopDetails(shopId)
            .mapCatching { enrichWithReviewAccess(it) }
            .onSuccess { details ->
                _uiState.update {
                    it.copy(details = details, isLoggedIn = isLoggedIn, isLoading = false)
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = if (showLoading) false else it.isLoading,
                        error = if (showLoading) e.message else it.error,
                    )
                }
            }
    }

    private suspend fun enrichWithReviewAccess(details: CoffeeShopDetails): CoffeeShopDetails {
        if (!sessionRepository.isLoggedIn()) {
            return details.copy(existingReviewId = null)
        }
        return reviewRepository.canCreateReview(shopId).fold(
            onSuccess = { (_, reviewId) -> details.copy(existingReviewId = reviewId) },
            onFailure = { details },
        )
    }

    fun toggleFavorite() {
        val details = _uiState.value.details ?: return
        val isFavorite = details.shop.isFavorite
        workScope.launch {
            _uiState.update { it.copy(isFavoriteLoading = true) }
            val result = if (isFavorite) {
                favoriteRepository.removeFavorite(shopId)
            } else {
                favoriteRepository.addFavorite(
                    shop = details.shop,
                    address = details.location?.address ?: details.shop.address,
                )
            }
            result
                .onSuccess {
                    val newFavoriteState = !isFavorite
                    FavoriteSync.notifyChanged(shopId, newFavoriteState)
                    _uiState.update { state ->
                        val current = state.details ?: return@update state
                        state.copy(
                            details = current.copy(
                                shop = current.shop.copy(isFavorite = newFavoriteState),
                            ),
                            isFavoriteLoading = false,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(actionMessage = e.message, isFavoriteLoading = false) }
                }
        }
    }

    fun openCheckInSheet() {
        val details = _uiState.value.details
        if (details?.isVisited == true) {
            _uiState.update { it.copy(actionMessage = "Вы уже отмечали это место") }
            return
        }
        _uiState.update { it.copy(showCheckInSheet = true) }
    }

    fun dismissCheckInSheet() {
        _uiState.update { it.copy(showCheckInSheet = false) }
    }

    fun checkIn(
        isPublic: Boolean,
        note: String?,
        placeRating: Int,
        serviceRating: Int,
        coffeeRating: Int,
    ) {
        if (isPublic && note.isNullOrBlank()) {
            _uiState.update { it.copy(actionMessage = "Для публичного чек-ина нужен комментарий") }
            return
        }
        workScope.launch {
            _uiState.update { it.copy(isCheckInLoading = true) }
            checkInRepository.createCheckIn(
                CreateCheckInInput(
                    shopId = shopId,
                    note = note,
                    visitedAtIso = currentUtcIsoDateTime(),
                    isPublic = isPublic,
                    placeRating = placeRating,
                    serviceRating = serviceRating,
                    coffeeRating = coffeeRating,
                ),
            ).onSuccess {
                _uiState.update { state ->
                    val current = state.details
                    state.copy(
                        details = current?.copy(isVisited = true),
                        isCheckInLoading = false,
                        showCheckInSheet = false,
                    )
                }
                refreshDetails(showLoading = false)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        actionMessage = e.message,
                        isCheckInLoading = false,
                    )
                }
            }
        }
    }

    fun openCreateReview() {
        workScope.launch {
            if (!sessionRepository.isLoggedIn()) {
                _uiState.update { it.copy(actionMessage = "Войдите, чтобы оставить отзыв") }
                return@launch
            }
            if (!_uiState.value.details?.existingReviewId.isNullOrBlank()) {
                _uiState.update { it.copy(actionMessage = "Вы уже оставляли отзыв об этом месте") }
                return@launch
            }
            Navigator.navigate(Navigator.Screen.CreateReview(shopId))
        }
    }

    fun openEditReview(reviewId: String) {
        Navigator.navigate(Navigator.Screen.ReviewEdit(reviewId))
    }

    fun openOnMap() {
        val details = _uiState.value.details ?: return
        val location = details.location ?: return
        val lat = location.latitude ?: return
        val lon = location.longitude ?: return
        Navigator.openShopOnMap(
            shopId = shopId,
            latitude = lat,
            longitude = lon,
            title = details.shop.title,
        )
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
}
