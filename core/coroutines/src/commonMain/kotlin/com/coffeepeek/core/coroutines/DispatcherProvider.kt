package com.coffeepeek.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Provides dispatchers used by application code without exposing a global dispatcher dependency. */
interface DispatcherProvider {
    val io: CoroutineDispatcher
}

/** Production dispatcher configuration. */
object DefaultDispatcherProvider : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
}

/** Creates an independent scope whose failures do not cancel sibling work. */
fun DispatcherProvider.supervisorScope(): CoroutineScope =
    CoroutineScope(SupervisorJob() + io)
