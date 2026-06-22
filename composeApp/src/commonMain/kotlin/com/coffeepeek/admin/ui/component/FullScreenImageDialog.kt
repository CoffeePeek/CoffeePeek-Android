package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.admin.utils.KamelExt

@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            KamelExt.FlowerImage(
                data = imageUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(CpDimens.spacing3),
            ) {
                Icon(
                    imageVector = CpIcons.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
