@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.coffeepeek.admin.ui.screen.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.coffeepeek.admin.AppContent
import com.coffeepeek.admin.theme.ThemeManager
import com.coffeepeek.admin.theme.ThemeMode
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.ProvideFloatingNavClearance
import com.coffeepeek.admin.ui.screen.feed.FeedScreen
import com.coffeepeek.admin.ui.screen.map.MapScreen
import com.coffeepeek.admin.ui.screen.profile.ProfileScreen
import com.coffeepeek.admin.ui.screen.profile.SettingsScreen
import platform.CoreGraphics.CGRectGetMaxY
import platform.CoreGraphics.CGRectGetMinY
import platform.UIKit.UIImage
import platform.UIKit.UITabBarController
import platform.UIKit.UITabBarControllerDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIViewController
import platform.UIKit.tabBarItem
import platform.darwin.NSObject

/** UIKit owns tabs and their animations; the existing root Navigator owns detail routes. */
@Composable
actual fun MainScreen() {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val pendingTab by Navigator.pendingTabSelection.collectAsState()
    val themeMode by ThemeManager.themeMode.collectAsState()

    LaunchedEffect(pendingTab) {
        pendingTab?.let { tab ->
            nativeTabIndex(tab)?.let { selectedIndex = it }
            Navigator.consumeTabSelection()
        }
    }
    LaunchedEffect(Unit) {
        Navigator.navigationEvents.collect { event ->
            if (event is Navigator.NavEvent.NavigateTo) {
                nativeTabIndex(event.screen)?.let { selectedIndex = it }
            }
        }
    }

    UIKitViewController(
        factory = { CoffeePeekTabController { selectedIndex = it } },
        modifier = Modifier.fillMaxSize(),
        update = {
            if (it.selectedIndex.toInt() != selectedIndex) {
                it.selectedIndex = selectedIndex.toULong()
            }
            it.overrideUserInterfaceStyle = when (themeMode) {
                ThemeMode.SYSTEM -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
                ThemeMode.LIGHT -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
                ThemeMode.DARK -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
            }
        },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
        ),
    )
}

private fun nativeTabIndex(screen: Navigator.Screen): Int? = when (screen) {
    Navigator.Screen.FeedTab, Navigator.Screen.FeedGraph -> 0
    Navigator.Screen.MapTab, Navigator.Screen.MapGraph -> 1
    Navigator.Screen.ProfileTab, Navigator.Screen.ProfileGraph -> 2
    Navigator.Screen.SettingsTab, Navigator.Screen.SettingsGraph -> 3
    else -> null
}

private class CoffeePeekTabController(onSelection: (Int) -> Unit) : UITabBarController(null, null) {
    private var bottomClearance by mutableStateOf(0.dp)

    // UIKit holds its delegate weakly, so retain it for the controller's lifetime.
    private val selectionDelegate = object : NSObject(), UITabBarControllerDelegateProtocol {
        override fun tabBarController(tabBarController: UITabBarController, didSelectViewController: UIViewController) {
            onSelection(tabBarController.selectedIndex.toInt())
        }
    }

    init {
        delegate = selectionDelegate
        setViewControllers(
            listOf(
                tab("Кофейни", "cup.and.saucer", 0) { FeedScreen() },
                tab("Карта", "map", 1) { MapScreen() },
                tab("Профиль", "person.crop.circle", 2) { ProfileScreen() },
                tab("Настройки", "gearshape", 3) { SettingsScreen() },
            ),
            animated = false,
        )
        // Keep the system appearance: iOS 26+ supplies Liquid Glass automatically.
    }

    private fun tab(title: String, symbol: String, tag: Long, content: @Composable () -> Unit): UIViewController =
        ComposeUIViewController {
            AppContent {
                ProvideFloatingNavClearance(bottomClearance) { content() }
            }
        }.apply {
            tabBarItem = UITabBarItem(title, UIImage.systemImageNamed(symbol), tag)
        }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        val contentView = selectedViewController?.view ?: return
        val barFrame = tabBar.convertRect(tabBar.bounds, toView = contentView)
        val overlap = (CGRectGetMaxY(contentView.bounds) - CGRectGetMinY(barFrame)).coerceAtLeast(0.0)
        bottomClearance = (overlap + 8.0).toFloat().dp
    }
}
