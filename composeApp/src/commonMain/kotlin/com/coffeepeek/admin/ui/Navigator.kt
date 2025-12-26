package com.coffeepeek.admin.ui

import com.coffeepeek.admin.ui.screen.auth.registr.RegisterScreen
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
import com.coffeepeek.admin.theme.Theme
import com.coffeepeek.admin.ui.dialogs.ErrorDialog
import com.coffeepeek.admin.ui.dialogs.LoadingDialog
import com.coffeepeek.admin.ui.screen.auth.AuthScreen
import com.coffeepeek.admin.ui.screen.main.MainScreen
import com.coffeepeek.admin.utils.ErrorHandler
import com.coffeepeek.admin.utils.LoadingHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

object Navigator {

    private val navigatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _navigationEvents = MutableSharedFlow<NavEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    sealed interface NavEvent {
        data class NavigateTo(val screen: Screen) : NavEvent
        data class SelectTab(val tab: Screen) : NavEvent
        data object PopBack : NavEvent
        data object NavigateUp : NavEvent
    }

    @Serializable
    sealed interface Screen {
        //Экраны вне навигейшн бара, прописывать здесь + BaseNavigator + Navigator.navigationEvents в MainScreen
        @Serializable data object Auth : Screen
        @Serializable data object Register : Screen
        @Serializable data object Main : Screen

        //ГРАФЫ
        @Serializable data object FeedGraph : Screen
        @Serializable data object MapGraph : Screen
        @Serializable data object VacanciesGraph : Screen
        @Serializable data object ProfileGraph : Screen

        //Табы
        @Serializable data object FeedTab : Screen
        @Serializable data object MapTab : Screen
        @Serializable data object VacanciesTab : Screen
        @Serializable data object ProfileTab : Screen

        //Внутренние экраны(добавлять для перехода сюда + в граф в MainScreen)
        @Serializable data class EditItem(val itemID: String?) : Screen
    }

    fun navigate(screen: Screen) {
        navigatorScope.launch { _navigationEvents.emit(NavEvent.NavigateTo(screen)) }
    }

    fun selectTab(tab: Screen) {
        navigatorScope.launch { _navigationEvents.emit(NavEvent.SelectTab(tab)) }
    }

    fun popBack() {
        navigatorScope.launch { _navigationEvents.emit(NavEvent.PopBack) }
    }

    @Composable
    operator fun invoke(
        vm: NavigatorViewModel = viewModel { NavigatorViewModel() }
    ) {
        val errorMessage = ErrorHandler.errorMessage.collectAsState().value
        val loading = LoadingHandler.isLoading.collectAsState().value
        val user = vm.user

        ErrorDialog(
            show = errorMessage != null,
            message = errorMessage ?: "",
            onDismiss = { ErrorHandler.clearError() }
        )
        LoadingDialog(show = loading)

        val startDestination = if (user?.userID.isNullOrEmpty()) Screen.Auth else Screen.Main

        BaseNavigator(startDestination)
    }

    @Composable
    private fun BaseNavigator(startDestination: Screen) {
        val nav = rememberNavController()

        LaunchedEffect(Unit) {
            navigationEvents.onEach { event ->
                when (event) {
                    is NavEvent.PopBack -> nav.popBackStack()
                    is NavEvent.NavigateUp -> nav.navigateUp()
                    is NavEvent.NavigateTo -> {
                        when (event.screen) {
                            Screen.Auth, Screen.Register, Screen.Main -> nav.navigate(event.screen)
                            else -> {
//                                if (nav.currentDestination?.route != Screen.Main::class.qualifiedName) { }
                            }
                        }
                    }
                    else -> Unit
                }
            }.launchIn(this)
        }

        NavHost(
            navController = nav,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize().background(brush = Theme.brush)
        ) {
            composable<Screen.Auth> { AuthScreen() }
            composable<Screen.Register> { RegisterScreen() }
            composable<Screen.Main> { MainScreen() }
        }
    }
}