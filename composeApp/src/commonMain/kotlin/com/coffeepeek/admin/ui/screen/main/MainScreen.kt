package com.coffeepeek.admin.ui.screen.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.screen.feed.FeedScreen
import com.coffeepeek.admin.ui.screen.map.MapScreen
import com.coffeepeek.admin.ui.screen.profile.ProfileScreen

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val graph: Navigator.Screen,
    val startScreen: Navigator.Screen,
)

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()

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
                    val handledByRoot = event.screen is Navigator.Screen.Auth ||
                        event.screen is Navigator.Screen.Main ||
                        event.screen is Navigator.Screen.Register ||
                        event.screen is Navigator.Screen.ShopDetail ||
                        event.screen is Navigator.Screen.AddShop ||
                        event.screen is Navigator.Screen.EditProfile
                    if (!handledByRoot) {
                        bottomNavController.navigate(event.screen)
                    }
                }
                else -> Unit
            }
        }
    }

    val items = listOf(
        BottomNavItem(
            title = "Лента",
            icon = Icons.Outlined.LocalCafe,
            graph = Navigator.Screen.FeedGraph,
            startScreen = Navigator.Screen.FeedTab,
        ),
        BottomNavItem(
            title = "Карта",
            icon = Icons.Default.Map,
            graph = Navigator.Screen.MapGraph,
            startScreen = Navigator.Screen.MapTab,
        ),
        BottomNavItem(
            title = "Профиль",
            icon = Icons.Default.Person,
            graph = Navigator.Screen.ProfileGraph,
            startScreen = Navigator.Screen.ProfileTab,
        ),
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { destination ->
                        destination.hasRoute(item.graph::class)
                    } == true

                    NavigationBarItem(
                        selected = isSelected,
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = MaterialTheme.colorScheme.primary,
                            selectedTextColor   = MaterialTheme.colorScheme.primary,
                            indicatorColor      = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        onClick = {
                            if (isSelected) {
                                bottomNavController.navigate(item.startScreen) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                bottomNavController.navigate(item.graph) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Navigator.Screen.FeedGraph,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            enterTransition = { fadeIn(tween(150)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { fadeOut(tween(150)) },
        ) {
            navigation<Navigator.Screen.FeedGraph>(startDestination = Navigator.Screen.FeedTab) {
                composable<Navigator.Screen.FeedTab> { FeedScreen() }
            }

            navigation<Navigator.Screen.MapGraph>(startDestination = Navigator.Screen.MapTab) {
                composable<Navigator.Screen.MapTab> { MapScreen() }
            }

            navigation<Navigator.Screen.ProfileGraph>(startDestination = Navigator.Screen.ProfileTab) {
                composable<Navigator.Screen.ProfileTab> { ProfileScreen() }
            }
        }
    }
}

