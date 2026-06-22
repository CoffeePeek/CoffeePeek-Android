package com.coffeepeek.admin.base

import androidx.lifecycle.ViewModel
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.email_no_exist
import coffeepeek.composeapp.generated.resources.maybe_later
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.ErrorHandler
import com.coffeepeek.admin.utils.LoadingHandler
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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

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

    /**
     * Универсальный метод для выполнения сетевых запросов.
     * @param errorMessage Текст ошибки. Если null — выведется дефолтная ошибка
     * @param onSuccess Лямбда, которая выполнится при успехе
     * @param request Сам запрос
     */
    protected fun <T> launchRequest(
        onSuccess: (T) -> Unit = {},
        errorMessage: StringResource? = null,
        request: suspend () -> T
    ) {
        workScope.launch {
            try {
                LoadingHandler.showLoading()

                val result = request()

                LoadingHandler.clearLoading()
                onSuccess(result)

            } catch (e: Exception) {
                LoadingHandler.clearLoading()
                e.printStackTrace()

                val messageToShow = getString(errorMessage ?: Res.string.maybe_later)

                ErrorHandler.showError(messageToShow)
            }
        }
    }

    override fun close() {
        workScope.cancel()
    }

}