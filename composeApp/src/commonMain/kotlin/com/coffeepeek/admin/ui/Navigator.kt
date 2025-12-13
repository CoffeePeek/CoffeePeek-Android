package com.coffeepeek.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.coffeepeek.admin.theme.Theme
import com.coffeepeek.admin.ui.dialogs.ErrorDialog
import com.coffeepeek.admin.ui.screen.auth.AuthScreen
import com.coffeepeek.admin.ui.screen.home.HomeScreen
import com.coffeepeek.admin.utils.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

object Navigator {

    private val navigatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val navigate = MutableSharedFlow<Screen>()


    @Serializable
    sealed interface Screen {

        @Serializable
        data object PopBack : Screen

        @Serializable
        data object NavigateUp : Screen

        @Serializable
        data class Chain(val first: Screen, val second: Screen) : Screen

        @Serializable
        data object Auth : Screen

        @Serializable
        data object Home : Screen

        @Serializable
        data object Profile : Screen

        @Serializable
        data class EditItem(val itemID: String?) : Screen

    }

    fun navigate(screen: Screen) {
        navigatorScope.launch { navigate.emit(screen) }
    }

    fun popBack() {
        navigatorScope.launch { navigate(Screen.PopBack) }
    }

    fun navigateUp() {
        navigatorScope.launch { navigate(Screen.NavigateUp) }
    }

    @Composable
    operator fun invoke(
        vm: NavigatorViewModel = viewModel { NavigatorViewModel() }
    ) {
        val errorMessage = ErrorHandler.errorMessage.collectAsState().value
        ErrorDialog(
            show = errorMessage != null,
            message = errorMessage ?: "",
            onDismiss = { ErrorHandler.clearError() }
        )
        val user = vm.user.collectAsState().value
        if (user == null) AuthScreen()
        else BaseNavigator()
    }

    @Composable
    private fun BaseNavigator() {
        val nav = rememberNavController()
        LaunchedEffect(Unit) {
            navigate
                .onEach {
                    when (it) {
                        is Screen.NavigateUp -> nav.navigateUp()
                        is Screen.PopBack -> nav.popBackStack()
                        is Screen.Chain -> {
                            when (it.first) {
                                is Screen.NavigateUp -> nav.popBackStack()
                                is Screen.PopBack -> nav.popBackStack()
                                else -> nav.navigate(it.first)
                            }
                            navigatorScope.launch { navigate.emit(it.second) }
                        }

                        else -> nav.navigate(it)
                    }
                }.launchIn(this)
        }
        NavHost(
            navController = nav,
            startDestination = Screen.Home,
            modifier = Modifier.fillMaxSize().background(brush = Theme.brush)
        ) {
            composable<Screen.Home> { HomeScreen() }
        }
    }

}