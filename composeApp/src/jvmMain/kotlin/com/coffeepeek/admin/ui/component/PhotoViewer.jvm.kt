package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.ic_add_24px
import coffeepeek.composeapp.generated.resources.ic_delete_24px
import coffeepeek.composeapp.generated.resources.ic_hide_image_24px
import com.coffeepeek.admin.theme.Colors
import com.coffeepeek.admin.utils.DrawableExt.toPainterResource
import com.coffeepeek.admin.utils.KamelExt
import kotlinx.coroutines.launch

actual object PhotoViewer {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    actual operator fun invoke(
        images: List<String>,
        isEdit: Boolean,
        onAdd: () -> Unit,
        onDelete: (String) -> Unit,
        preferredItemWidth: Dp,
        itemSpacing: Dp,
        cardModifier: Modifier,
        modifier: Modifier,
    ) {
        val size  = if (isEdit) images.size + 1 else images.size
        val itemCount = size.let { { it } }
        val state = rememberCarouselState(itemCount = itemCount)
        val scope = rememberCoroutineScope()
        Box(
            modifier = Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { scope.launch { state.stopScroll() } },
                    onDragStart = { }
                ) { f, s -> scope.launch { state.scrollBy(-s.x) } }
            }
        ) {
            if (size > 0) Box {
                HorizontalMultiBrowseCarousel(
                    state = state,
                    preferredItemWidth = preferredItemWidth,
                    itemSpacing = itemSpacing,
                    modifier = modifier
                ) {
                    if (it <= images.lastIndex) {
                        val item = images[it]
                        Box(modifier = cardModifier){
                            KamelExt.FlowerImage(
                                data = item,
                                contentScale = ContentScale.Crop,
                                placeholder = Res.drawable.ic_hide_image_24px,
                                modifier = cardModifier
                            )
                            if (isEdit) Buttons.IconButton(
                                backgroundColor = Colors.errorColor,
                                painter = Res.drawable.ic_delete_24px.toPainterResource(),
                                onClick =  { onDelete(item) }
                            )
                        }
                    } else if (isEdit) Box(modifier = cardModifier.clickable(onClick = onAdd)) {
                        Icon(
                            painter = Res.drawable.ic_add_24px.toPainterResource(),
                            contentDescription = null,
                            modifier = Modifier.size(200.dp).align(Alignment.Center).clickable(onClick = onAdd)
                        )
                    }
                }
            }
        }
    }
}