package com.coffeepeek.core.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class MviViewModelTest {
    private data class CounterState(val count: Int = 0)
    private data object Increment

    private class CounterViewModel : MviViewModel<CounterState, Increment, Nothing>(CounterState()) {
        override fun onAction(action: Increment) {
            updateState { copy(count = count + 1) }
        }
    }

    private class EventViewModel : MviViewModel<CounterState, Increment, String>(CounterState()) {
        override fun onAction(action: Increment) = Unit
        suspend fun announce(message: String) = sendEvent(message)
    }

    @Test fun typedStateUpdatesAreAtomic() = runBlocking {
        val viewModel = CounterViewModel()
        val jobs = List(100) { launch(Dispatchers.Default) { viewModel.onAction(Increment) } }
        jobs.joinAll()

        assertEquals(100, viewModel.state.value.count)
    }

    @Test fun oneOffEventsKeepEmissionOrder() = runBlocking {
        val viewModel = EventViewModel()
        viewModel.announce("first")
        viewModel.announce("second")

        assertEquals("first", viewModel.events.first())
        assertEquals("second", viewModel.events.first())
    }
}
