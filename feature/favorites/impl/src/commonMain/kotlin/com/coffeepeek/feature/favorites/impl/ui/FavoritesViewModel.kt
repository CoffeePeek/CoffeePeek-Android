package com.coffeepeek.feature.favorites.impl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeepeek.feature.favorites.domain.FavoritesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(FavoritesUiState())
    val state = mutableState.asStateFlow()
    private var observation: Job? = null
    private var generation = 0

    init { retry() }

    fun retry() {
        val current = ++generation
        observation?.cancel()
        mutableState.update { it.copy(isLoading = it.shops.isEmpty(), loadFailed = false, actionFailed = false) }
        observation = viewModelScope.launch {
            repository.observe().collect { result ->
                result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
                if (current == generation) mutableState.update {
                    it.copy(shops = result.getOrNull() ?: it.shops, isLoading = false, loadFailed = result.isFailure)
                }
            }
        }
    }

    fun remove(shopId: String) {
        if (shopId in state.value.removing || state.value.shops.none { it.id == shopId }) return
        mutableState.update { it.copy(removing = it.removing + shopId, actionFailed = false) }
        viewModelScope.launch {
            try {
                val result = repository.remove(shopId)
                result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
                // Observation owns the list. Failed writes never optimistically erase cards.
                mutableState.update { it.copy(actionFailed = result.isFailure) }
            } finally {
                mutableState.update { it.copy(removing = it.removing - shopId) }
            }
        }
    }
}
