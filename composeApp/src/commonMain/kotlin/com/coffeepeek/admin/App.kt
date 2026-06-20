package com.coffeepeek.admin

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.coffeepeek.admin.theme.CoffeePeekTheme
import com.coffeepeek.admin.theme.ThemeManager
import com.coffeepeek.admin.theme.ThemeMode
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.OrientationObserver
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    OrientationObserver.StartObserver()

    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
    }

    CoffeePeekTheme(darkTheme = darkTheme) {
        Navigator()
    }
}
