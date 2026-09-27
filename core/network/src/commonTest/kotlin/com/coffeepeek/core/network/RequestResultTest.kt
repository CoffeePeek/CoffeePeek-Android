package com.coffeepeek.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertFailsWith

class RequestResultTest {
    @Test
    fun returnsValue(): Unit = runBlocking {
        assertEquals(42, requestResult { 42 }.getOrThrow())
    }

    @Test
    fun preservesFailure(): Unit = runBlocking {
        val failure = IllegalStateException("request failed")
        assertSame(failure, requestResult<Int> { throw failure }.exceptionOrNull())
    }

    @Test
    fun cancellationIsNotConvertedToFailure(): Unit = runBlocking {
        val cancellation = CancellationException("cancelled")
        assertSame(cancellation, assertFailsWith<CancellationException> {
            requestResult<Int> { throw cancellation }
        })
    }
}
