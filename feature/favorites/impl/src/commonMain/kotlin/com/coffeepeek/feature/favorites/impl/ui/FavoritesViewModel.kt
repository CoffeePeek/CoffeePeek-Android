package com.coffeepeek.feature.favorites.impl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeepeek.feature.favorites.domain.repository.FavoritesRepository
import com.coffeepeek.feature.favorites.impl.ui.compose.model.FavoritesAction
import com.coffeepeek.feature.favorites.impl.ui.compose.model.FavoritesEvent
import com.coffeepeek.feature.favorites.impl.ui.compose.model.FavoritesUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(FavoritesUiState())
    val state = mutableState.asStateFlow()
    private val eventChannel = Channel<FavoritesEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    private var observation: Job? = null
    private var generation = 0

    init { retry() }

    fun onAction(action: FavoritesAction) {
        when (action) {
            FavoritesAction.Retry -> retry()
            is FavoritesAction.Remove -> remove(action.shopId)
            is FavoritesAction.OpenShop -> viewModelScope.launch {
                eventChannel.send(FavoritesEvent.OpenShop(action.shopId))
            }
            FavoritesAction.Back -> viewModelScope.launch { eventChannel.send(FavoritesEvent.Back) }
        }
    }

    private fun retry() {
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

    private fun remove(shopId: String) {
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
