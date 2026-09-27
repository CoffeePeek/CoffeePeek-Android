package com.coffeepeek.core.network

import kotlinx.coroutines.CancellationException

/** Request boundaries return failures while preserving structured cancellation. */
suspend fun <T> requestResult(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (failure: Exception) {
    Result.failure(failure)
}
