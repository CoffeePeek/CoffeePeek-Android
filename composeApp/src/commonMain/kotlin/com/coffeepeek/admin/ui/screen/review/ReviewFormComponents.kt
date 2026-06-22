package com.coffeepeek.admin.ui.screen.review

import com.coffeepeek.admin.ui.icons.CpIcons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpDimens
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun ReviewHeaderField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Заголовок") },
        singleLine = true,
    )
}

@Composable
fun ReviewCommentField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().height(140.dp),
        label = { Text("Текст отзыва") },
    )
}

@Composable
fun ReviewRatingRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { star ->
                IconButton(onClick = { onChange(star) }) {
                    Icon(
                        imageVector = if (star <= value) CpIcons.StarFilled else CpIcons.StarOutline,
                        contentDescription = null,
                        tint = if (star <= value) MaterialTheme.colorScheme.primary else Color.Gray,
                    )
                }
            }
        }
    }
}

@Composable
fun ExistingReviewPhotos(photoUrls: List<String>, onPhotoClick: (String) -> Unit) {
    if (photoUrls.isEmpty()) return
    Column {
        Text(
            text = "Текущие фото",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = CpDimens.spacing2),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(CpDimens.spacing2)) {
            photoUrls.forEach { url ->
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(CpDimens.radiusSm))
                        .clickable { onPhotoClick(url) },
                ) {
                    KamelImage(
                        resource = asyncPainterResource(url),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
