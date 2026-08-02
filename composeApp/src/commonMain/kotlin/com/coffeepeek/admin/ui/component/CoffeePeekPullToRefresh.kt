package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpDimens
import kotlin.math.min

@Composable
fun CoffeePeekPullToRefresh(
    listState: LazyListState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (scrollModifier: Modifier) -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 72.dp.toPx() }
    var pullOffset by remember { mutableFloatStateOf(0f) }
    var lastRefreshAt by remember { mutableLongStateOf(0L) }

    val isRefreshingState by rememberUpdatedState(isRefreshing)
    val listStateState by rememberUpdatedState(listState)
    val onRefreshState by rememberUpdatedState(onRefresh)

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero

                val currentListState = listStateState
                val atTop = currentListState.firstVisibleItemIndex == 0 &&
                    currentListState.firstVisibleItemScrollOffset == 0

                if (!atTop) {
                    if (pullOffset > 0f) pullOffset = 0f
                    return Offset.Zero
                }
                if (available.y > 0f && !isRefreshingState) {
                    val damped = available.y * 0.5f
                    val next = (pullOffset + damped).coerceAtMost(thresholdPx * 1.4f)
                    val consumedY = (next - pullOffset) / 0.5f
                    pullOffset = next
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0f && pullOffset > 0f) {
                    val release = min(-available.y, pullOffset / 0.5f)
                    pullOffset = (pullOffset - release * 0.5f).coerceAtLeast(0f)
                    return Offset(0f, -release)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val now = System.currentTimeMillis()
                if (
                    pullOffset >= thresholdPx &&
                    !isRefreshingState &&
                    now - lastRefreshAt > 1_000L
                ) {
                    lastRefreshAt = now
                    onRefreshState()
                }
                pullOffset = 0f
                return Velocity.Zero
            }
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) pullOffset = 0f
    }

    val scrollModifier = Modifier.nestedScroll(nestedScrollConnection)
    val showIndicator = isRefreshing || pullOffset > 0f

    Box(modifier = modifier) {
        if (showIndicator) {
            val progress = if (isRefreshing) 1f else (pullOffset / thresholdPx).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = CpDimens.spacing2)
                    .graphicsLayer { alpha = progress },
                contentAlignment = Alignment.TopCenter,
            ) {
                CoffeePeekLoader(
                    size = CpDimens.loaderButton,
                    strokeWidth = 2.dp,
                )
            }
        }
        content(scrollModifier)
    }
}
