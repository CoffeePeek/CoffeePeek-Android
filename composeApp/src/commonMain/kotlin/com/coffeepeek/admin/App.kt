package com.coffeepeek.admin

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
fun App(onReady: () -> Unit = {}) {
    val kamelConfig = koinInject<KamelConfig>()
    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
    }

    LaunchedEffect(Unit) {
        onReady()
    }

    CoffeePeekTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
            Box(Modifier.fillMaxSize()) {
                OrientationObserver.StartObserver()
                Navigator()
            }
        }
    }
}
