package com.coffeepeek.admin.ui.screen.shop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coffeepeek.admin.di.platformViewModel
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.component.CoffeePeekLoader
import com.coffeepeek.admin.ui.component.CoffeeShopImage
import com.coffeepeek.admin.ui.component.CpTopBar
import com.coffeepeek.admin.ui.component.FullScreenImageDialog
import org.koin.core.parameter.parametersOf

@Composable
fun ShopMenuGalleryScreen(shopId: String) {
    val vm: ShopMenuGalleryViewModel = platformViewModel(parameters = { parametersOf(shopId) })
    val state by vm.uiState.collectAsState()
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    previewImageUrl?.let { url ->
        FullScreenImageDialog(
            imageUrl = url,
            onDismiss = { previewImageUrl = null },
        )
    }

    Scaffold(
        topBar = { CpTopBar(title = "Фотографии меню") },
        containerColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CoffeePeekLoader()
                state.error != null -> GalleryError(
                    message = state.error.orEmpty(),
                    onRetry = vm::load,
                )
                state.photos.isEmpty() -> Text(
                    text = "Фотографий меню пока нет",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = CpDimens.spacing4,
                        top = CpDimens.spacing2,
                        end = CpDimens.spacing4,
                        bottom = CpDimens.spacing6,
                    ),
                    verticalArrangement = Arrangement.spacedBy(CpDimens.spacing4),
                ) {
                    if (state.shopTitle.isNotBlank()) {
                        item {
                            Text(
                                text = state.shopTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    itemsIndexed(
                        items = state.photos,
                        key = { index, photo -> "${photo.id}-${photo.fullUrl}-$index" },
                    ) { index, photo ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.72f)
                                .clip(RoundedCornerShape(CpDimens.radiusLg))
                                .clickable { previewImageUrl = photo.fullUrl },
                        ) {
                            CoffeeShopImage(
                                imageUrl = photo.fullUrl,
                                contentDescription = "Фотография меню ${index + 1}",
                                contentScale = ContentScale.Fit,
                                placeholderLabelSize = 18.sp,
                                modifier = Modifier.fillMaxSize(),
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.Black.copy(alpha = 0.48f),
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(CpDimens.spacing3),
                            ) {
                                Text(
                                    text = "${index + 1} / ${state.photos.size}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(
                                        horizontal = CpDimens.spacing3,
                                        vertical = CpDimens.spacing1,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryError(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing3),
        modifier = Modifier.padding(CpDimens.spacing4),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}
