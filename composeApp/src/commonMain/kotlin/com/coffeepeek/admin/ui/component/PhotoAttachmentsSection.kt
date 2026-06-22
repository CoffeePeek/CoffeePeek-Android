package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.icons.CpIcons
import com.coffeepeek.admin.utils.KamelExt
import com.coffeepeek.admin.utils.PickedImage
import com.coffeepeek.admin.utils.rememberPhotoPicker

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoAttachmentsSection(
    photos: List<PickedImage>,
    maxPhotos: Int,
    onPhotosAdded: (List<PickedImage>) -> Unit,
    onRemovePhoto: (Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Фотографии",
    hint: String = "Добавьте до $maxPhotos фото (необязательно).",
) {
    var isPhotoLoading by remember { mutableStateOf(false) }
    val remaining = (maxPhotos - photos.size).coerceAtLeast(1)
    val photoPicker = rememberPhotoPicker(
        maxSelection = remaining,
        isLoading = { isPhotoLoading = it },
        onPhotosPicked = onPhotosAdded,
    )

    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(CpDimens.spacing1))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(CpDimens.spacing2))
        Text(
            text = "Добавлено: ${photos.size}/$maxPhotos",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(CpDimens.spacing2))

        if (photos.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
                modifier = Modifier.padding(bottom = CpDimens.spacing2),
            ) {
                photos.forEachIndexed { index, photo ->
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(CpDimens.radiusMd)),
                    ) {
                        KamelExt.FlowerImage(
                            data = photo.bytes,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        IconButton(
                            onClick = { onRemovePhoto(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50)),
                        ) {
                            Icon(
                                CpIcons.Close,
                                contentDescription = "Удалить",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
        }

        if (photos.size < maxPhotos) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
            ) {
                OutlinedButton(
                    onClick = photoPicker.pickFromGallery,
                    enabled = !isPhotoLoading,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(CpIcons.Gallery, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(CpDimens.spacing1))
                    Text("Галерея", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = photoPicker.takePhoto,
                    enabled = !isPhotoLoading,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(CpIcons.Camera, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(CpDimens.spacing1))
                    Text("Камера", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (isPhotoLoading) {
                Spacer(Modifier.height(CpDimens.spacing2))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CoffeePeekLoader(size = CpDimens.loaderButton, strokeWidth = 2.dp)
                }
            }
        }
    }
}
