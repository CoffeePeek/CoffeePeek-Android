package com.coffeepeek.admin

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.coffeepeek.admin.theme.CoffeePeekTheme
import com.coffeepeek.admin.theme.ThemeManager
import com.coffeepeek.admin.theme.ThemeMode
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.OrientationObserver
import io.kamel.core.config.KamelConfig
import io.kamel.image.config.LocalKamelConfig
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

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

    val kamelConfig = koinInject<KamelConfig>()

    CoffeePeekTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
            Navigator()
        }
    }
}
