package com.coffeepeek.admin.ui.screen.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

object HomeScreen {


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        vm: HomeViewModel = viewModel { HomeViewModel() }
    ) {
    }
}