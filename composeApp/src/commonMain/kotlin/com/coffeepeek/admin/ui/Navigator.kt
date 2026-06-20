package com.coffeepeek.admin.ui

import com.coffeepeek.admin.ui.screen.addshop.AddShopScreen
import com.coffeepeek.admin.ui.screen.auth.registr.RegisterScreen
import com.coffeepeek.admin.ui.screen.editprofile.EditProfileScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.coffeepeek.admin.theme.Theme
import com.coffeepeek.admin.ui.dialogs.ErrorDialog
import com.coffeepeek.admin.ui.dialogs.LoadingDialog
import com.coffeepeek.admin.ui.screen.auth.AuthScreen
import com.coffeepeek.admin.ui.screen.main.MainScreen
import com.coffeepeek.admin.ui.screen.shop.ShopDetailScreen
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
        // Root screens (outside bottom nav)
        @Serializable data object Auth : Screen
        @Serializable data object Register : Screen
        @Serializable data object Main : Screen

        // Graphs
        @Serializable data object FeedGraph : Screen
        @Serializable data object MapGraph : Screen
        @Serializable data object ProfileGraph : Screen

        // Tabs
        @Serializable data object FeedTab : Screen
        @Serializable data object MapTab : Screen
        @Serializable data object ProfileTab : Screen

        // Inner screens (add here + in the graph in MainScreen)
        @Serializable data class ShopDetail(val shopId: String) : Screen
        @Serializable data class EditItem(val itemID: String?) : Screen
        @Serializable data object AddShop : Screen
        @Serializable data object EditProfile : Screen
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
        vm: NavigatorViewModel = koinViewModel(),
    ) {
        val errorMessage = ErrorHandler.errorMessage.collectAsState().value
        val loading = LoadingHandler.isLoading.collectAsState().value
        val isLoggedIn = vm.isLoggedIn.collectAsState().value

        ErrorDialog(
            show = errorMessage != null,
            message = errorMessage ?: "",
            onDismiss = { ErrorHandler.clearError() }
        )
        LoadingDialog(show = loading)

        val startDestination = if (isLoggedIn) Screen.Main else Screen.Auth

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
                    is NavEvent.NavigateTo -> nav.navigate(event.screen)
                    else -> Unit
                }
            }.launchIn(this)
        }

        NavHost(
            navController = nav,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<Screen.Auth> { AuthScreen() }
            composable<Screen.Register> { RegisterScreen() }
            composable<Screen.Main> { MainScreen() }
            composable<Screen.ShopDetail> { backStack ->
                val route = backStack.toRoute<Screen.ShopDetail>()
                ShopDetailScreen(shopId = route.shopId)
            }
            composable<Screen.AddShop> { AddShopScreen() }
            composable<Screen.EditProfile> { EditProfileScreen() }
        }
    }
}
