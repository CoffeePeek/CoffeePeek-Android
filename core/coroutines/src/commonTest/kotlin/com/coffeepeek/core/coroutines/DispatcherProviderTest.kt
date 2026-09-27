package com.coffeepeek.core.coroutines

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers

class DispatcherProviderTest {

    @Test
    fun defaultProviderUsesIoDispatcher() {
        assertEquals(Dispatchers.IO, DefaultDispatcherProvider.io)
    }
}
