package com.coffeepeek.admin.ui.screen.auth

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.locator.Locator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel: BaseViewModel() {

    private val authService = Locator.flowerRepo.authService

    init {
        workScope.launch {
            while (true){
                println("PRINT ")
                delay(1000)
            }
        }
    }

    fun next() {
        workScope.launch {
           authService.protected().getOrNull()
        }
    }

}