package com.coffeepeek.admin.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

@Composable
expect inline fun <reified T : ViewModel> platformViewModel(
    qualifier: Qualifier? = null,
    key: String? = null,
    noinline parameters: ParametersDefinition? = null,
): T
