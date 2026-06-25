package com.coffeepeek.admin.utils

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.image_not_available
import com.coffeepeek.admin.locator.Constants
import com.coffeepeek.admin.utils.DrawableExt.toPainterResource
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun CpImage(
    data: Any,
    placeholder: DrawableResource? = null,
    error: DrawableResource? = Res.drawable.image_not_available,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val resolvedData = when {
        data is String -> when {
            data.startsWith("https://") || data.startsWith("http://") -> CustomUrl(data)
            else -> CustomUrl("${Constants.BASE_URL}api/file/$data")
        }
        else -> data
    }
    Image(
        painter = rememberCpImagePainter(resolvedData, placeholder, error),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}

@Composable
fun rememberCpImagePainter(
    resource: Any,
    placeholder: DrawableResource? = null,
    error: DrawableResource? = null,
): androidx.compose.ui.graphics.painter.Painter {
    val loadingPlaceholder = placeholder?.toPainterResource()
        ?: ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    return when (val r = asyncPainterResource(data = resource)) {
        is Resource.Failure -> error?.toPainterResource() ?: loadingPlaceholder
        is Resource.Loading -> loadingPlaceholder
        is Resource.Success -> r.value
    }
}
