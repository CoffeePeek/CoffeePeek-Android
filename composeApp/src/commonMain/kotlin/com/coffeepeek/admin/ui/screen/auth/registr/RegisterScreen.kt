package com.coffeepeek.admin.ui.screen.auth.registr

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.enter
import coffeepeek.composeapp.generated.resources.error_enter_password_length
import coffeepeek.composeapp.generated.resources.have_account
import coffeepeek.composeapp.generated.resources.join_community
import coffeepeek.composeapp.generated.resources.name
import coffeepeek.composeapp.generated.resources.name_hint
import coffeepeek.composeapp.generated.resources.password
import coffeepeek.composeapp.generated.resources.registr
import com.coffeepeek.admin.theme.Colors
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.admin.ui.component.AppButton
import com.coffeepeek.admin.ui.component.AppTextField
import org.jetbrains.compose.resources.stringResource

object RegisterScreen {

    @Composable
    operator fun invoke(
        vm: RegisterViewModel = viewModel { RegisterViewModel() }
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
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Colors.brandColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalCafe,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("CoffeePeek", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                stringResource(Res.string.join_community),
                fontSize = 14.sp,
                color = Colors.textGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.registr),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AppTextField(
                        label = stringResource(Res.string.name),
                        value = name,
                        onValueChange = vm::onNameChange,
                        placeholder = stringResource(Res.string.name_hint),
                        leadingIcon = Icons.Outlined.Person
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        label = "Email",
                        value = email,
                        onValueChange = vm::onEmailChange,
                        placeholder = "your@email.com",
                        leadingIcon = Icons.Outlined.Email,
                        errorText = emailError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        label = stringResource(Res.string.password),
                        value = password,
                        onValueChange = vm::onPasswordChange,
                        placeholder = "••••••••",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        errorText = passwordError
                    )

                    if (passwordError == null) {
                        Text(
                            text = stringResource(Res.string.error_enter_password_length),
                            fontSize = 12.sp,
                            color = Colors.textGray,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isTermsAccepted,
                            onCheckedChange = vm::onTermsCheckedChange,
                            colors = CheckboxDefaults.colors(checkedColor = Colors.brandColor),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TermsText()
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = stringResource(Res.string.registr),
                        onClick = { vm.onRegisterClick() },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Text(
                            stringResource(Res.string.have_account),
                            color = Colors.brandColor,
                            fontSize = 14.sp
                        )
                        Text(
                            stringResource(Res.string.enter),
                            color = Colors.brandColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { Navigator.popBack() }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    OrContinueWithDivider()
                    Spacer(modifier = Modifier.height(20.dp))
                    GoogleButton()
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TermsText() {
    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = Colors.brandColor,
            fontWeight = FontWeight.Medium
        )
    )

    val annotatedString = buildAnnotatedString {
        append("Я принимаю ")
        val termsLink = LinkAnnotation.Url(
            url = "https://plugins.jetbrains.com/plugin/9960-json-to-kotlin-class-jsontokotlinclass-/versions/stable",
            styles = linkStyles
        )
        withLink(termsLink) {
            append("условия использования")
        }

        append(" и ")

        val privacyLink = LinkAnnotation.Url(
            url = "https://www.youtube.com/",
            styles = linkStyles
        )
        withLink(privacyLink) {
            append("политику конфиденциальности")
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall.copy(
            color = Colors.textGray,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    )
}

@Composable
private fun OrContinueWithDivider() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
        Text(
            " или продолжить с ",
            fontSize = 12.sp,
            color = Colors.textGray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
    }
}

@Composable
private fun GoogleButton() {
    OutlinedButton(
        onClick = { },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Text(
            "G",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text("Google")
    }
}