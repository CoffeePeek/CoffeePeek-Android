package com.coffeepeek.admin

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.coffeepeek.admin.di.initPlatformKoin

fun main() = application {
    initPlatformKoin()
    val windowState = rememberWindowState(
        size = DpSize(800.dp, 800.dp)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "CoffeePeek",
        state = windowState
    ) {
        App()
    }
}