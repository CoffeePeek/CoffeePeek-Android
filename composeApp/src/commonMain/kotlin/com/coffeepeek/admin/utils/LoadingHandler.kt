package com.coffeepeek.admin.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LoadingHandler {

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun showLoading() {
        _isLoading.value = true
    }

    fun clearLoading() {
        _isLoading.value = false
    }
}