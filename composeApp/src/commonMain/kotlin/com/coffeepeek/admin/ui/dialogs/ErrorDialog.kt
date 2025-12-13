package com.coffeepeek.admin.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.coffeepeek.admin.theme.Colors
import com.coffeepeek.admin.theme.Theme

object ErrorDialog {

    @Composable
    operator fun invoke(
        show: Boolean,
        message: String,
        onDismiss: () -> Unit
    ) {
        AnimatedVisibility(
            visible = show
        ) {
            Dialog(
                onDismissRequest = onDismiss
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Theme.shape)
                        .background(Colors.cardBackground)
                        .padding(Theme.horizontalPadding)
                ) {
                    Column(
                        modifier = Modifier.padding(Theme.verticalPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ошибка",
                            modifier = Modifier.padding(bottom = Theme.verticalPadding)
                        )
                        Text(
                            text = message,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = Theme.verticalPadding)
                        )
                        Button(
                            onClick = onDismiss
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }

}

