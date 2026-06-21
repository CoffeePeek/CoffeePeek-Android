package com.coffeepeek.admin.ui.screen.shop

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.CreateCheckInInput
import com.coffeepeek.domain.repository.CheckInRepository
import com.coffeepeek.domain.repository.FavoriteRepository
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.coffeepeek.admin.utils.FavoriteSync
import com.coffeepeek.admin.utils.currentUtcIsoDateTime

data class ShopDetailUiState(
    val details: CoffeeShopDetails? = null,
    val isLoading: Boolean = false,
    val isFavoriteLoading: Boolean = false,
    val isCheckInLoading: Boolean = false,
    val actionMessage: String? = null,
    val error: String? = null,
)

class ShopDetailViewModel(
    private val shopId: String,
    private val shopRepository: ShopRepository,
    private val favoriteRepository: FavoriteRepository,
    private val checkInRepository: CheckInRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ShopDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        workScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            refreshDetails(showLoading = true)
        }
    }

    private suspend fun refreshDetails(showLoading: Boolean) {
        shopRepository.getShopDetails(shopId)
            .onSuccess { details ->
                _uiState.update { it.copy(details = details, isLoading = false) }
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

    fun toggleFavorite() {
        val details = _uiState.value.details ?: return
        val isFavorite = details.shop.isFavorite
        workScope.launch {
            _uiState.update { it.copy(isFavoriteLoading = true) }
            val result = if (isFavorite) {
                favoriteRepository.removeFavorite(shopId)
            } else {
                favoriteRepository.addFavorite(shopId)
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

    fun checkIn(note: String?) {
        workScope.launch {
            _uiState.update { it.copy(isCheckInLoading = true) }
            checkInRepository.createCheckIn(
                CreateCheckInInput(
                    shopId = shopId,
                    note = note,
                    visitedAtIso = currentUtcIsoDateTime(),
                    isPublic = false,
                )
            ).onSuccess {
                _uiState.update { state ->
                    val current = state.details
                    state.copy(
                        details = current?.copy(isVisited = true),
                        isCheckInLoading = false,
                    )
                }
                refreshDetails(showLoading = false)
            }.onFailure { e ->
                _uiState.update { it.copy(actionMessage = e.message, isCheckInLoading = false) }
            }
        }
    }

    fun openCreateReview() {
        Navigator.navigate(Navigator.Screen.CreateReview(shopId))
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
