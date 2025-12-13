package com.coffeepeek.admin.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.coffeepeek.admin.theme.Colors
import com.coffeepeek.admin.theme.Theme

object LoadingDialog {


    @Composable
    operator fun invoke(show: Boolean) {
        AnimatedVisibility(
            visible = show
        ) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(70.dp)
                        .clip(Theme.shape)
                        .background(Colors.cardBackground)
                        .padding(Theme.horizontalPadding)
                ) {
                    CircularProgressIndicator(
                        color = Colors.greenDark
                    )
                }
            }
        }
    }

}