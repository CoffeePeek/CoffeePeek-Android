package com.coffeepeek.admin.ui.screen.auth

import com.coffeepeek.admin.ui.component.AppButton
import com.coffeepeek.admin.ui.component.AppTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.app_name
import coffeepeek.composeapp.generated.resources.eco_system
import coffeepeek.composeapp.generated.resources.entry
import coffeepeek.composeapp.generated.resources.login
import coffeepeek.composeapp.generated.resources.no_account
import coffeepeek.composeapp.generated.resources.password
import coffeepeek.composeapp.generated.resources.registr
import com.coffeepeek.admin.theme.Colors
import com.coffeepeek.admin.ui.Navigator
import org.jetbrains.compose.resources.stringResource

object AuthScreen {

    @Composable
    operator fun invoke(
        vm: AuthViewModel = viewModel { AuthViewModel() }
    ) {
        val email by vm.email.collectAsState()
        val password by vm.password.collectAsState()
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


            Text(
                stringResource(Res.string.app_name),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(Res.string.eco_system), fontSize = 14.sp, color = Colors.textGray)

            Spacer(modifier = Modifier.height(24.dp))

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
                        text = stringResource(Res.string.entry),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AppTextField(
                        label = "Email",
                        value = email,
                        onValueChange = vm::onEmailChange,
                        placeholder = "name@example.com",
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
                        isPassword = true
                    )


                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = stringResource(Res.string.login),
                        onClick = { vm.onLoginClick() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Text(
                            stringResource(Res.string.no_account),
                            color = Colors.brandColor,
                            fontSize = 14.sp
                        )
                        Text(
                            stringResource(Res.string.registr),
                            color = Colors.brandColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { Navigator.navigate(Navigator.Screen.Register) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    OrContinueWithDivider()
                    Spacer(modifier = Modifier.height(20.dp))
                    GoogleButton()
                }
            }
        }
    }
}

@Composable
private fun OrContinueWithDivider() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            " или продолжить с ",
            fontSize = 12.sp,
            color = Colors.textGray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun GoogleButton() {
    OutlinedButton(
        onClick = { },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
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