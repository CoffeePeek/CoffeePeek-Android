package com.coffeepeek.admin

import androidx.compose.ui.window.ComposeUIViewController
import com.coffeepeek.admin.di.initPlatformKoin
import platform.UIKit.UIViewController

private var koinInitialized = false

fun MainViewController(): UIViewController {
    if (!koinInitialized) {
        initPlatformKoin()
        koinInitialized = true
    }
    return ComposeUIViewController { App() }
}
