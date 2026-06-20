package com.coffeepeek.admin.ui.screen.editprofile

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.Navigator
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(vm: EditProfileViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()

    // Диалог ошибки
    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = vm::clearError,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Ошибка", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(err, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                TextButton(onClick = vm::clearError) {
                    Text("Понятно", style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = RoundedCornerShape(CpDimens.radius2xl),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Редактировать профиль", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = { Navigator.popBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(end = 4.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(CpDimens.spacing3))
                    } else {
                        TextButton(
                            onClick = vm::save,
                            enabled = state.canSave,
                        ) {
                            Text(
                                "Сохранить",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (state.canSave)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(CpDimens.spacing4),
        ) {
            // ── Имя пользователя ─────────────────────────────────────────────
            FieldLabel("Имя пользователя")
            OutlinedTextField(
                value = state.username,
                onValueChange = vm::onUsernameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Ваше имя",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                isError = state.username.isNotEmpty() && state.usernameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                shape = RoundedCornerShape(CpDimens.radiusMd),
                colors = fieldColors(),
            )
            FieldFooter(
                error = if (state.username.isNotEmpty()) state.usernameError else null,
                counter = "${state.username.length}/64",
            )

            Spacer(Modifier.height(CpDimens.spacing4))

            // ── О себе ───────────────────────────────────────────────────────
            FieldLabel("О себе")
            OutlinedTextField(
                value = state.about,
                onValueChange = vm::onAboutChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Расскажите немного о себе…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                isError = state.aboutError != null,
                minLines = 4,
                maxLines = 8,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default,
                ),
                shape = RoundedCornerShape(CpDimens.radiusMd),
                colors = fieldColors(),
            )
            FieldFooter(
                error = state.aboutError,
                counter = "${state.about.length}/600",
            )

            Spacer(Modifier.height(CpDimens.spacing6))

            // ── Кнопка сохранения ─────────────────────────────────────────────
            Button(
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth().height(CpDimens.buttonHeight),
                enabled = state.canSave,
                shape = RoundedCornerShape(CpDimens.buttonRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                ),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(CpDimens.loaderButton),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Сохранить изменения", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(CpDimens.spacing8))
        }
    }
}

// ── Вспомогательные компоненты ────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
    )
}

@Composable
private fun FieldFooter(error: String?, counter: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        if (counter != null) {
            Text(
                text = counter,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor    = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor  = MaterialTheme.colorScheme.outline,
    errorBorderColor      = MaterialTheme.colorScheme.error,
    focusedContainerColor    = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor  = MaterialTheme.colorScheme.surface,
)
