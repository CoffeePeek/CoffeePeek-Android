package com.coffeepeek.admin.ui

import com.coffeepeek.admin.ui.screen.addshop.AddShopScreen
import com.coffeepeek.admin.ui.screen.auth.registr.RegisterScreen
import com.coffeepeek.admin.ui.screen.brew.BeanEditScreen
import com.coffeepeek.admin.ui.screen.brew.BeanListScreen
import com.coffeepeek.admin.ui.screen.brew.BrewDetailScreen
import com.coffeepeek.admin.ui.screen.brew.BrewHomeScreen
import com.coffeepeek.admin.ui.screen.brew.BrewOriginsScreen
import com.coffeepeek.admin.ui.screen.brew.BrewTrendsScreen
import com.coffeepeek.admin.ui.screen.brew.NewBrewScreen
import com.coffeepeek.admin.ui.screen.checkins.VisitedPlacesScreen
import com.coffeepeek.admin.ui.screen.editprofile.EditProfileScreen
import com.coffeepeek.admin.ui.screen.favorites.FavoritesScreen
import com.coffeepeek.admin.ui.screen.reviews.MyReviewsScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.coffeepeek.admin.ui.dialogs.ErrorDialog
import com.coffeepeek.admin.ui.dialogs.LoadingDialog
import com.coffeepeek.admin.ui.screen.auth.AuthScreen
import com.coffeepeek.admin.ui.screen.main.MainScreen
import com.coffeepeek.admin.ui.screen.map.MapScreen
import com.coffeepeek.admin.ui.screen.review.CreateReviewScreen
import com.coffeepeek.admin.ui.screen.review.EditReviewScreen
import com.coffeepeek.admin.ui.screen.shop.ShopDetailScreen
import com.coffeepeek.admin.utils.ErrorHandler
import com.coffeepeek.admin.utils.LoadingHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
        @Serializable data object Map : Screen

        // Graphs
        @Serializable data object FeedGraph : Screen
        @Serializable data object ProfileGraph : Screen

        // Tabs
        @Serializable data object FeedTab : Screen
        @Serializable data object ProfileTab : Screen

        // Inner screens (add here + in the graph in MainScreen)
        @Serializable data class ShopDetail(val shopId: String) : Screen
        @Serializable data object AddShop : Screen
        @Serializable data object EditProfile : Screen
        @Serializable data object Favorites : Screen
        @Serializable data object MyReviews : Screen
        @Serializable data object VisitedPlaces : Screen
        @Serializable data class CreateReview(val shopId: String) : Screen
        @Serializable data class ReviewEdit(val reviewId: String) : Screen

        // Brew journal (local-first)
        @Serializable data object BrewHome : Screen
        @Serializable data class NewBrew(val repeatSessionId: String = "") : Screen
        @Serializable data class BrewDetail(val sessionId: String) : Screen
        @Serializable data object BrewBeans : Screen
        @Serializable data class BeanEdit(val beanId: String = "") : Screen
        @Serializable data object BrewTrends : Screen
        @Serializable data object BrewOrigins : Screen
    }

    data class MapShopFocus(
        val shopId: String,
        val latitude: Double,
        val longitude: Double,
        val title: String,
    )

    private val _pendingMapFocus = MutableStateFlow<MapShopFocus?>(null)
    val pendingMapFocus = _pendingMapFocus.asStateFlow()

    private val _pendingTabSelection = MutableStateFlow<Screen?>(null)
    val pendingTabSelection = _pendingTabSelection.asStateFlow()

    fun consumeMapFocus() {
        _pendingMapFocus.value = null
    }

    fun consumeTabSelection() {
        _pendingTabSelection.value = null
    }

    fun openShopOnMap(shopId: String, latitude: Double, longitude: Double, title: String) {
        navigatorScope.launch {
            _pendingMapFocus.value = MapShopFocus(shopId, latitude, longitude, title)
            _navigationEvents.emit(NavEvent.NavigateTo(Screen.Map))
        }
    }

    fun Screen.isHandledByRootNav(): Boolean = when (this) {
        is Screen.Auth,
        is Screen.Register,
        is Screen.Main,
        is Screen.Map,
        is Screen.ShopDetail,
        is Screen.AddShop,
        is Screen.EditProfile,
        is Screen.CreateReview,
        is Screen.ReviewEdit,
        is Screen.Favorites,
        is Screen.MyReviews,
        is Screen.VisitedPlaces,
        is Screen.BrewHome,
        is Screen.NewBrew,
        is Screen.BrewDetail,
        is Screen.BrewBeans,
        is Screen.BeanEdit,
        is Screen.BrewTrends,
        is Screen.BrewOrigins -> true
        else -> false
    }

    fun navigate(screen: Screen) {
        navigatorScope.launch { _navigationEvents.emit(NavEvent.NavigateTo(screen)) }
    }

    fun selectTab(tab: Screen) {
        navigatorScope.launch {
            _pendingTabSelection.value = tab
            _navigationEvents.emit(NavEvent.SelectTab(tab))
        }
    }

    fun popBack() {
        navigatorScope.launch { _navigationEvents.emit(NavEvent.PopBack) }
    }

    /** Pop current screen, then navigate (ordered in one coroutine). */
    fun popThenNavigate(screen: Screen) {
        navigatorScope.launch {
            _navigationEvents.emit(NavEvent.PopBack)
            _navigationEvents.emit(NavEvent.NavigateTo(screen))
        }
    }

    @Composable
    operator fun invoke(
        vm: NavigatorViewModel = koinViewModel(),
    ) {
        val errorMessage = ErrorHandler.errorMessage.collectAsState().value
        val loading = LoadingHandler.isLoading.collectAsState().value
        val isLoggedIn by vm.isLoggedIn.collectAsState()

        ErrorDialog(
            show = errorMessage != null,
            message = errorMessage ?: "",
            onDismiss = { ErrorHandler.clearError() }
        )
        LoadingDialog(show = loading)

        LaunchedEffect(isLoggedIn) {
            if (!isLoggedIn) {
                ErrorHandler.clearError()
                LoadingHandler.clearLoading()
            }
        }

        BaseNavigator(isLoggedIn = isLoggedIn)
    }

    @Composable
    private fun BaseNavigator(isLoggedIn: Boolean) {
        val nav = rememberNavController()

        LaunchedEffect(Unit) {
            navigationEvents.onEach { event ->
                when (event) {
                    is NavEvent.PopBack -> nav.popBackStack()
                    is NavEvent.NavigateUp -> nav.navigateUp()
                    is NavEvent.NavigateTo -> {
                        if (event.screen.isHandledByRootNav()) {
                            nav.navigate(event.screen)
                        }
                    }
                    else -> Unit
                }
            }.launchIn(this)
        }

        key(isLoggedIn) {
            NavHost(
                navController = nav,
                startDestination = if (isLoggedIn) Screen.Main else Screen.Auth,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable<Screen.Auth> { AuthScreen() }
                composable<Screen.Register> { RegisterScreen() }
                composable<Screen.Main> { MainScreen() }
                composable<Screen.Map> { MapScreen() }
                composable<Screen.ShopDetail> { backStack ->
                    val route = backStack.toRoute<Screen.ShopDetail>()
                    ShopDetailScreen(shopId = route.shopId)
                }
                composable<Screen.AddShop> { AddShopScreen() }
                composable<Screen.EditProfile> { EditProfileScreen() }
                composable<Screen.CreateReview> { backStack ->
                    val route = backStack.toRoute<Screen.CreateReview>()
                    CreateReviewScreen(shopId = route.shopId)
                }
                composable<Screen.ReviewEdit> { backStack ->
                    val route = backStack.toRoute<Screen.ReviewEdit>()
                    EditReviewScreen(reviewId = route.reviewId)
                }
                composable<Screen.Favorites> { FavoritesScreen() }
                composable<Screen.MyReviews> { MyReviewsScreen() }
                composable<Screen.VisitedPlaces> { VisitedPlacesScreen() }
                composable<Screen.BrewHome> { BrewHomeScreen() }
                composable<Screen.NewBrew> { backStack ->
                    val route = backStack.toRoute<Screen.NewBrew>()
                    NewBrewScreen(repeatSessionId = route.repeatSessionId)
                }
                composable<Screen.BrewDetail> { backStack ->
                    val route = backStack.toRoute<Screen.BrewDetail>()
                    BrewDetailScreen(sessionId = route.sessionId)
                }
                composable<Screen.BrewBeans> { BeanListScreen() }
                composable<Screen.BeanEdit> { backStack ->
                    val route = backStack.toRoute<Screen.BeanEdit>()
                    BeanEditScreen(beanId = route.beanId)
                }
                composable<Screen.BrewTrends> { BrewTrendsScreen() }
                composable<Screen.BrewOrigins> { BrewOriginsScreen() }
            }
        }
    }
}
