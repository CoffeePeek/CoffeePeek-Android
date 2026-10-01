package com.coffeepeek.admin.ui.screen.shop

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.domain.model.ShopMenuPhoto
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopMenuGalleryUiState(
    val shopTitle: String = "",
    val photos: List<ShopMenuPhoto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ShopMenuGalleryViewModel(
    private val shopId: String,
    private val shopRepository: ShopRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ShopMenuGalleryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        workScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            shopRepository.getShopDetails(shopId)
                .onSuccess { details ->
                    _uiState.update {
                        it.copy(
                            shopTitle = details.shop.title,
                            photos = details.menu?.photos.orEmpty(),
                            isLoading = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Не удалось загрузить фотографии меню",
                        )
                    }
                }
        }
    }
}
