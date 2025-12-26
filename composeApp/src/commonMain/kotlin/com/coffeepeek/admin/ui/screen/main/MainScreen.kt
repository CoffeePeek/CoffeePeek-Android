package com.coffeepeek.admin.ui.screen.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.coffeepeek.admin.theme.Colors
import com.coffeepeek.admin.ui.Navigator

@Composable fun FeedScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Лента") } }
@Composable fun MapScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Карта") } }
@Composable fun VacanciesScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Вакансии") } }

@Composable fun ProfileScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {
            Navigator.navigate(Navigator.Screen.EditItem("123"))
        }) {
            Text("Редактировать профиль")
        }
    }
}

@Composable fun EditItemScreen(id: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Редактирование Item: $id")
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val graph: Navigator.Screen,
    val startScreen: Navigator.Screen
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
                    if (event.screen !is Navigator.Screen.Auth &&
                        event.screen !is Navigator.Screen.Main &&
                        event.screen !is Navigator.Screen.Register) {

                        bottomNavController.navigate(event.screen)
                    }
                }
                else -> Unit
            }
        }
    }

    val items = listOf(
        BottomNavItem(
            "Лента",
            Icons.Outlined.LocalCafe,
            graph = Navigator.Screen.FeedGraph,
            startScreen = Navigator.Screen.FeedTab
        ),
        BottomNavItem(
            "Карта",
            Icons.Default.Map,
            graph = Navigator.Screen.MapGraph,
            startScreen = Navigator.Screen.MapTab
        ),
        BottomNavItem(
            "Вакансии",
            Icons.Default.Work,
            graph = Navigator.Screen.VacanciesGraph,
            startScreen = Navigator.Screen.VacanciesTab
        ),
        BottomNavItem(
            "Профиль",
            Icons.Default.Person,
            graph = Navigator.Screen.ProfileGraph,
            startScreen = Navigator.Screen.ProfileTab
        ),
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { destination ->
                        destination.hasRoute(item.graph::class)
                    } == true

                    NavigationBarItem(
                        selected = isSelected,
                        label = { Text(item.title) },
                        icon = { Icon(item.icon, null) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFB54B00),
                            selectedTextColor = Color(0xFFB54B00),
                            indicatorColor = Colors.lightYellowBg
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
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Navigator.Screen.FeedGraph,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(150)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { fadeOut(tween(150)) }
        ) {
            //Здесь прописываем переход для конкретного графа
            navigation<Navigator.Screen.FeedGraph>(startDestination = Navigator.Screen.FeedTab) {
                composable<Navigator.Screen.FeedTab> { FeedScreen() }
            }

            navigation<Navigator.Screen.MapGraph>(startDestination = Navigator.Screen.MapTab) {
                composable<Navigator.Screen.MapTab> { MapScreen() }
            }

            navigation<Navigator.Screen.VacanciesGraph>(startDestination = Navigator.Screen.VacanciesTab) {
                composable<Navigator.Screen.VacanciesTab> { VacanciesScreen() }
            }

            navigation<Navigator.Screen.ProfileGraph>(startDestination = Navigator.Screen.ProfileTab) {
                composable<Navigator.Screen.ProfileTab> { ProfileScreen() }

                composable<Navigator.Screen.EditItem> {
                    EditItemScreen(null)
                }
            }
        }
    }
}