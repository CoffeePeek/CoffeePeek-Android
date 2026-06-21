package com.coffeepeek.admin.ui.screen.auth

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.app_name
import coffeepeek.composeapp.generated.resources.eco_system
import coffeepeek.composeapp.generated.resources.entry
import coffeepeek.composeapp.generated.resources.login
import coffeepeek.composeapp.generated.resources.no_account
import coffeepeek.composeapp.generated.resources.password
import coffeepeek.composeapp.generated.resources.registr
import com.coffeepeek.admin.auth.GoogleSignInButton
import com.coffeepeek.admin.auth.isGoogleSignInConfigured
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.AppButton
import com.coffeepeek.admin.ui.component.AppTextField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

object AuthScreen {

    @Composable
    operator fun invoke(
        vm: AuthViewModel = koinViewModel(),
    ) {
        val email by vm.email.collectAsState()
        val password by vm.password.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(CpDimens.spacing6),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(CpDimens.spacing10))

            // ── Логотип ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .size(CpDimens.headerLogoSize)
                    .clip(RoundedCornerShape(CpDimens.radiusMd))
                    .background(MaterialTheme.colorScheme.primary),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "CP",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Spacer(modifier = Modifier.height(CpDimens.spacing4))

            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(Res.string.eco_system),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(CpDimens.spacing8))

            // ── Карточка формы ────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(CpDimens.cardRadius),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(CpDimens.cardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.entry),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = CpDimens.spacing4),
                    )

                    AppTextField(
                        label = "Email",
                        value = email,
                        onValueChange = vm::onEmailChange,
                        placeholder = "name@example.com",
                        leadingIcon = CpIcons.Email,
                    )

                    Spacer(modifier = Modifier.height(CpDimens.spacing3))

                    AppTextField(
                        label = stringResource(Res.string.password),
                        value = password,
                        onValueChange = vm::onPasswordChange,
                        placeholder = "••••••••",
                        leadingIcon = CpIcons.Lock,
                        isPassword = true,
                    )

                    Spacer(modifier = Modifier.height(CpDimens.spacing6))

                    AppButton(
                        text = stringResource(Res.string.login),
                        onClick = { vm.onLoginClick() },
                    )

                    Spacer(modifier = Modifier.height(CpDimens.spacing4))

                    Row {
                        Text(
                            text = stringResource(Res.string.no_account),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = " " + stringResource(Res.string.registr),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                Navigator.navigate(Navigator.Screen.Register)
                            },
                        )
                    }

                    if (isGoogleSignInConfigured()) {
                        Spacer(modifier = Modifier.height(CpDimens.spacing5))
                        OrDivider()
                        Spacer(modifier = Modifier.height(CpDimens.spacing5))
                        GoogleSignInButton(
                            onResult = { result ->
                                result.onSuccess(vm::onGoogleLogin)
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(CpDimens.spacing8))
        }
    }
}

@Composable
private fun OrDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            text = "  или  ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
