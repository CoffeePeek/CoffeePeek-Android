package com.coffeepeek.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** A modal progress surface. The screen owns visibility and work cancellation. */
@Composable
fun LoadingDialog(show: Boolean, loadingDescription: String, text: String? = null) {
    if (!show) return
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CoffeePeekLoader(contentDescription = loadingDescription)
                if (text != null) {
                    Text(text, style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}
