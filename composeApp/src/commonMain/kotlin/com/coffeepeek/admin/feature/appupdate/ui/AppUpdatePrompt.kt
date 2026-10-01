package com.coffeepeek.admin.feature.appupdate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.coffeepeek.admin.config.AppConfig
import com.coffeepeek.admin.utils.OpenInBrowser
import org.koin.compose.koinInject

@Composable
internal fun AppUpdatePrompt(controller: AppUpdateState = koinInject()) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { controller.check() }
    val state by controller.state.collectAsState()
    val update = state.update ?: return
    if (!state.showPrompt) return
    val required = update.isRequired(AppConfig.versionCode ?: return)
    val action = when (AppConfig.updateChannel) {
        "play" -> "Перейти в Play Market"
        "apk" -> "Скачать новую версию"
        else -> "Перейти в App Store"
    }
    val open = {
        try { OpenInBrowser.openInBrowser(update.url) } catch (_: Exception) { controller.openingFailed() }
    }
    val title = if (required) "Необходимо обновить CoffeePeek" else "Вышло обновление CoffeePeek"
    val description = if (required) "Для продолжения установите новую версию приложения." else
        "Доступна новая версия приложения. Обновите CoffeePeek, чтобы получить последние улучшения."
    if (required) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false),
        ) {
            Surface(Modifier.fillMaxSize()) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(title, style = MaterialTheme.typography.headlineSmall)
                    Text(description)
                    state.openingError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = open) { Text(action) }
                }
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = controller::dismiss,
            title = { Text(title) },
            text = { Column { Text(description); state.openingError?.let { Text(it) } } },
            confirmButton = { TextButton(onClick = open) { Text(action) } },
            dismissButton = { TextButton(onClick = controller::dismiss) { Text("Позже") } },
        )
    }
}
