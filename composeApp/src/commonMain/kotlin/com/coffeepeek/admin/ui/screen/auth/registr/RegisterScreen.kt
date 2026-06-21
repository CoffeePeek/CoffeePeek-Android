package com.coffeepeek.admin.ui.screen.auth.registr

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.enter
import coffeepeek.composeapp.generated.resources.error_enter_password_length
import coffeepeek.composeapp.generated.resources.have_account
import coffeepeek.composeapp.generated.resources.join_community
import coffeepeek.composeapp.generated.resources.name
import coffeepeek.composeapp.generated.resources.name_hint
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

object RegisterScreen {

    @Composable
    operator fun invoke(
        vm: RegisterViewModel = koinViewModel(),
    ) {
        val name by vm.name.collectAsState()
        val email by vm.email.collectAsState()
        val password by vm.password.collectAsState()
        val isTermsAccepted by vm.isTermsAccepted.collectAsState()
        val passwordError by vm.passwordError.collectAsState()
        val emailError by vm.emailError.collectAsState()

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
                text = "CoffeePeek",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(Res.string.join_community),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(CpDimens.spacing8))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(CpDimens.cardRadius),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(CpDimens.cardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.registr),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = CpDimens.spacing4),
                    )

                    AppTextField(
                        label = stringResource(Res.string.name),
                        value = name,
                        onValueChange = vm::onNameChange,
                        placeholder = stringResource(Res.string.name_hint),
                        leadingIcon = CpIcons.User,
                    )

                    Spacer(modifier = Modifier.height(CpDimens.spacing3))

                    AppTextField(
                        label = "Email",
                        value = email,
                        onValueChange = vm::onEmailChange,
                        placeholder = "your@email.com",
                        leadingIcon = CpIcons.Email,
                        errorText = emailError,
                    )

                    Spacer(modifier = Modifier.height(CpDimens.spacing3))

                    AppTextField(
                        label = stringResource(Res.string.password),
                        value = password,
                        onValueChange = vm::onPasswordChange,
                        placeholder = "••••••••",
                        leadingIcon = CpIcons.Lock,
                        isPassword = true,
                        errorText = passwordError,
                    )

                    if (passwordError == null) {
                        Text(
                            text = stringResource(Res.string.error_enter_password_length),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = CpDimens.inputPadding),
                            textAlign = TextAlign.Start,
                        )
                    }

                    Spacer(modifier = Modifier.height(CpDimens.spacing4))

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = isTermsAccepted,
                            onCheckedChange = vm::onTermsCheckedChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                            ),
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(CpDimens.spacing3))
                        TermsText()
                    }

                    Spacer(modifier = Modifier.height(CpDimens.spacing6))

                    AppButton(
                        text = stringResource(Res.string.registr),
                        onClick = { vm.onRegisterClick() },
                    )

                    Spacer(modifier = Modifier.height(CpDimens.spacing4))

                    Row {
                        Text(
                            text = stringResource(Res.string.have_account),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = " " + stringResource(Res.string.enter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { Navigator.popBack() },
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
            Spacer(modifier = Modifier.height(CpDimens.spacing6))
        }
    }
}

@Composable
private fun TermsText() {
    val primary = MaterialTheme.colorScheme.primary
    val linkStyles = TextLinkStyles(
        style = SpanStyle(color = primary),
    )

    val annotatedString = buildAnnotatedString {
        append("Я принимаю ")
        withLink(
            LinkAnnotation.Url(
                url = "https://coffeepeek.by/terms",
                styles = linkStyles,
            )
        ) { append("условия использования") }
        append(" и ")
        withLink(
            LinkAnnotation.Url(
                url = "https://coffeepeek.by/privacy",
                styles = linkStyles,
            )
        ) { append("политику конфиденциальности") }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun OrDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = "  или  ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}
