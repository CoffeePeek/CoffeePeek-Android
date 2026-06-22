package com.coffeepeek.admin.auth

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeepeek.BuildConfig
import com.coffeepeek.admin.theme.CpDimens

actual fun isGoogleSignInConfigured(): Boolean =
    BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

@Composable
actual fun GoogleSignInButton(
    onResult: (Result<String>) -> Unit,
    modifier: Modifier,
) {
    val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
    if (webClientId.isBlank()) return

    val launchSignIn = GoogleAuth.rememberGoogleSignInLauncher(
        webClientId = webClientId,
        onResult = onResult,
    )
    OutlinedButton(
        onClick = launchSignIn,
        modifier = modifier.fillMaxWidth().height(CpDimens.buttonHeight),
        shape = RoundedCornerShape(CpDimens.buttonRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(
            imageVector = CpIcons.Email,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = "  Google",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
