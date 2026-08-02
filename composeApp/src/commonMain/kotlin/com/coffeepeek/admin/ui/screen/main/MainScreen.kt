package com.coffeepeek.admin.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.Navigator.isHandledByRootNav
import com.coffeepeek.admin.ui.component.FloatingBottomNavBar
import com.coffeepeek.admin.ui.component.FloatingNavItem
import com.coffeepeek.admin.ui.component.ProvideFloatingNavClearance
import com.coffeepeek.admin.ui.screen.feed.FeedScreen
import com.coffeepeek.admin.ui.screen.profile.ProfileScreen
import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val graph: Navigator.Screen,
    val startScreen: Navigator.Screen,
)

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()
    val pendingTabSelection by Navigator.pendingTabSelection.collectAsState()

    LaunchedEffect(Unit) {
        Navigator.navigationEvents.collect { event ->
            when (event) {
                is Navigator.NavEvent.SelectTab -> {
                    bottomNavController.navigate(event.tab) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                is Navigator.NavEvent.NavigateTo -> {
                    if (!event.screen.isHandledByRootNav()) {
                        bottomNavController.navigate(event.screen)
                    }
                }
                else -> Unit
            }
        }
    }

    LaunchedEffect(pendingTabSelection) {
        pendingTabSelection?.let { tab ->
            bottomNavController.navigate(tab) {
                popUpTo(bottomNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            Navigator.consumeTabSelection()
        }
    }

    val items = listOf(
        BottomNavItem(
            title = "Кофейни",
            icon = CpIcons.Coffee,
            graph = Navigator.Screen.FeedGraph,
            startScreen = Navigator.Screen.FeedTab,
        ),
        BottomNavItem(
            title = "Профиль",
            icon = CpIcons.Profile,
            graph = Navigator.Screen.ProfileGraph,
            startScreen = Navigator.Screen.ProfileTab,
        ),
    )

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val density = LocalDensity.current
    val systemNavBottom = with(density) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }
    val floatingClearance = systemNavBottom +
        CpDimens.floatingNavContentClearance +
        CpDimens.floatingNavBottomMargin

    ProvideFloatingNavClearance(clearance = floatingClearance) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = bottomNavController,
                startDestination = Navigator.Screen.FeedGraph,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(150)) },
                exitTransition = { fadeOut(tween(150)) },
                popEnterTransition = { fadeIn(tween(150)) },
                popExitTransition = { fadeOut(tween(150)) },
            ) {
                navigation<Navigator.Screen.FeedGraph>(startDestination = Navigator.Screen.FeedTab) {
                    composable<Navigator.Screen.FeedTab> { FeedScreen() }
                }

                navigation<Navigator.Screen.ProfileGraph>(startDestination = Navigator.Screen.ProfileTab) {
                    composable<Navigator.Screen.ProfileTab> { ProfileScreen() }
                }
            }

            FloatingBottomNavBar(
                items = items.map { item ->
                    val isSelected = currentDestination?.hierarchy?.any { destination ->
                        destination.hasRoute(item.graph::class)
                    } == true
                    FloatingNavItem(
                        title = item.title,
                        icon = item.icon,
                        selected = isSelected,
                        onClick = {
                            if (isSelected) return@FloatingNavItem
                            bottomNavController.navigate(item.graph) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
