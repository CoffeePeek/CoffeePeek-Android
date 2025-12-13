package com.coffeepeek.admin.base

import androidx.lifecycle.ViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.handleError
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel(), Closeable {


    protected val workScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        addCloseable(this)
    }

    override fun onCleared() {
        workScope.cancel()
        super.onCleared()
    }

    fun <T> Flow<T>.stateHere(
        initialValue: T,
        started: SharingStarted = SharingStarted.Eagerly,
    ) = this.stateIn(workScope, started, initialValue)

    protected fun <T> Result<T>.handleError(): Result<T> = handleError()

    override fun close() {
        println("TAG CLOSE $this")
        workScope.cancel()
    }

}