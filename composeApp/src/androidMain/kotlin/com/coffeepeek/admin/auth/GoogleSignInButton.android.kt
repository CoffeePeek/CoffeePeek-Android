package com.coffeepeek.admin.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.ic_google_g
import coffeepeek.composeapp.generated.resources.sign_in_google
import com.coffeepeek.BuildConfig
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val gap = Modifier.width(12.dp)

    // Centered Google mark + label, matching the Figma auth button.
    OutlinedButton(
        onClick = launchSignIn,
        modifier = modifier
            .fillMaxWidth()
            .height(CpDimens.authPrimaryBtnHeight),
        shape = RoundedCornerShape(CpDimens.authFieldRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(horizontal = 14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isDark) CpColor.AuthGoogleBg else Color.White,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_google_g),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(gap)
            Text(
                text = stringResource(Res.string.sign_in_google),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}
