package com.coffeepeek.admin.utils

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.image_not_available
import com.coffeepeek.admin.locator.Constants
import com.coffeepeek.admin.locator.Locator
import com.coffeepeek.admin.utils.DrawableExt.toPainterResource
import io.kamel.core.Resource
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.asyncPainterResource
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.ktor.http.Url
import org.jetbrains.compose.resources.DrawableResource

object KamelExt {

    private val loadConfig = KamelConfig {
        takeFrom(KamelConfig.Default)
        fetcher(ByteArrayFetcher)
        fetcher(Locator.urlFetcher)
    }

    object EmptyPainter : Painter() {
        override fun DrawScope.onDraw() {}
        override val intrinsicSize: Size = Size(1f, 1f)
    }


    @Composable
    fun FlowerImage(
        data: Any,
        placeholder: DrawableResource? = null,
        error: DrawableResource? = Res.drawable.image_not_available,
        contentScale: ContentScale = ContentScale.Crop,
        modifier: Modifier = Modifier
    ) {
        CompositionLocalProvider(LocalKamelConfig.provides(loadConfig)) {
            val data = when {
                data is String -> when {
                    data.startsWith("https://") or data.startsWith("http://") -> CustomUrl(data)
                    else -> CustomUrl("${Constants.DOMAIN_URL}file/$data")
                }

                else -> data
            }
            val resource = load(data, placeholder, error)
            Image(
                painter = resource,
                contentDescription = null,
                contentScale = contentScale,
                modifier = modifier
            )
        }

    }

    @Composable
    fun load(
        resource: Any, placeholder: DrawableResource? = null, error: DrawableResource? = null
    ): Painter = when (val r = asyncPainterResource(data = resource)) {
        is Resource.Failure -> error?.toPainterResource() ?: EmptyPainter
        is Resource.Loading -> placeholder?.toPainterResource() ?: EmptyPainter
        is Resource.Success -> r.value
    }


}