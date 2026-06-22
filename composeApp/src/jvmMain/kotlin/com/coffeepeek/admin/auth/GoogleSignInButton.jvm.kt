package com.coffeepeek.admin.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual fun isGoogleSignInConfigured(): Boolean = false

@Composable
actual fun GoogleSignInButton(
    onResult: (Result<String>) -> Unit,
    modifier: Modifier,
) = Unit
