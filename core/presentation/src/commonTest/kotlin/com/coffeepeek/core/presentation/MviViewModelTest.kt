package com.coffeepeek.core.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MviViewModelTest {
    private data class CounterState(val count: Int = 0)
    private data object Increment

    private class CounterViewModel : MviViewModel<CounterState, Increment, Nothing>() {
        private val mutableState = MutableStateFlow(CounterState())
        override val state = mutableState.asStateFlow()

        override fun onAction(action: Increment) {
            mutableState.update { it.copy(count = it.count + 1) }
        }
    }

    @Test fun typedStateAndActionsWorkWithoutInventingEvents() = runBlocking {
        val viewModel = CounterViewModel()
        viewModel.onAction(Increment)

        assertEquals(1, viewModel.state.value.count)
        assertTrue(viewModel.events.toList().isEmpty())
    }
}
