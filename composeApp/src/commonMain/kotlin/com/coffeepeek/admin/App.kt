package com.coffeepeek.admin

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.utils.OrientationObserver
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    OrientationObserver.StartObserver()
    MaterialTheme {
        Navigator()
    }
}