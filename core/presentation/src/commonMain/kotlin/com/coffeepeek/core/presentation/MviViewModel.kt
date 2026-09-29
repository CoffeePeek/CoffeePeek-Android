package com.coffeepeek.core.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

/** Typed base for migrated feature ViewModels, without application-wide UI policy. */
abstract class MviViewModel<State : Any, Action : Any, Event : Any> : ViewModel() {
    abstract val state: StateFlow<State>
    open val events: Flow<Event> = emptyFlow()

    abstract fun onAction(action: Action)
}
