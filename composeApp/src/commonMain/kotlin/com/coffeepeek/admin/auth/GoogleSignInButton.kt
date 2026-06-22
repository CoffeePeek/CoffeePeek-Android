package com.coffeepeek.admin.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect fun isGoogleSignInConfigured(): Boolean

@Composable
expect fun GoogleSignInButton(
    onResult: (Result<String>) -> Unit,
    modifier: Modifier = Modifier,
)
