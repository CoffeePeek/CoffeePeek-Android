package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.coffeepeek.admin.theme.CpDimens

/**
 * Compact swipeable photo stack shared by menu, review, and check-in photos.
 * The cards behind the current page remain visible to make the swipe affordance explicit.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeablePhotoStack(
    itemCount: Int,
    itemWidth: Dp,
    itemHeight: Dp,
    onItemClick: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    peek: Dp = 12.dp,
    maxPeeks: Int = 2,
    content: @Composable BoxScope.(Int) -> Unit,
) {
    if (itemCount == 0) return

    val pagerState = rememberPagerState(pageCount = { itemCount })
    val visiblePeeks = (itemCount - 1).coerceIn(0, maxPeeks)
    val peekPx = with(LocalDensity.current) { peek.toPx() }
    val shape = RoundedCornerShape(CpDimens.radiusMd)

    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(itemWidth),
        beyondViewportPageCount = maxPeeks,
        modifier = modifier
            .width(itemWidth + peek * visiblePeeks)
            .height(itemHeight),
    ) { page ->
        val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val depth = (-offset).coerceAtLeast(0f)
        val visibleDepth = depth.coerceAtMost(maxPeeks.toFloat())
        Box(
            modifier = Modifier
                .zIndex(-depth)
                .graphicsLayer {
                    if (depth > 0f) {
                        translationX = visibleDepth * peekPx - depth * size.width
                        val scale = 1f - 0.08f * visibleDepth
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(1f, 0.5f)
                        alpha = (maxPeeks + 1 - depth).coerceIn(0f, 1f)
                    }
                }
                .fillMaxSize()
                .shadow(if (depth < 0.5f) 4.dp else 0.dp, shape)
                .clip(shape)
                .then(
                    if (onItemClick != null) Modifier.clickable { onItemClick(page) }
                    else Modifier,
                ),
        ) {
            content(page)
        }
    }
}
