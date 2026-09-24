package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.icons.CpIcons

@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
) = FullScreenImageDialog(imageUrls = listOf(imageUrl), initialIndex = 0, onDismiss = onDismiss)

/** Full-screen photo viewer; swipe horizontally between [imageUrls], tap anywhere to close. */
@Composable
fun FullScreenImageDialog(
    imageUrls: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    if (imageUrls.isEmpty()) return
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, imageUrls.lastIndex),
        pageCount = { imageUrls.size },
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                CoffeeShopImage(
                    imageUrl = imageUrls[page],
                    contentDescription = "Фото ${page + 1} из ${imageUrls.size}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss,
                        ),
                )
            }

            if (imageUrls.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${imageUrls.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = CpDimens.spacing4)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(percent = 50))
                        .padding(horizontal = CpDimens.spacing3, vertical = CpDimens.spacing1),
                )
            }

            GlassIconButton(
                onClick = onDismiss,
                contentDescription = "Закрыть",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(CpDimens.spacing3),
            ) {
                Icon(
                    imageVector = CpIcons.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
