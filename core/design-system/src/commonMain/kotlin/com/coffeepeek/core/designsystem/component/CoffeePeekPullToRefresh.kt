package com.coffeepeek.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.coffeepeek.core.designsystem.theme.CpDimens
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Attach the supplied modifier to the LazyColumn using [listState].
 * The caller owns refresh work, errors and [isRefreshing]; there are no requests here.
 */
@Composable
fun CoffeePeekPullToRefresh(
    listState: LazyListState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    refreshDescription: String,
    loadingDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    threshold: Dp = 72.dp,
    cooldown: Duration = 1.seconds,
    content: @Composable (scrollModifier: Modifier) -> Unit,
) {
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    val gesture = remember(thresholdPx, cooldown) {
        val origin = TimeSource.Monotonic.markNow()
        PullRefreshGestureState(thresholdPx, cooldown) { origin.elapsedNow() }
    }
    val refreshingNow by rememberUpdatedState(isRefreshing)
    val enabledNow by rememberUpdatedState(enabled)
    val listNow by rememberUpdatedState(listState)
    val refreshNow by rememberUpdatedState(onRefresh)
    fun atTop() = listNow.firstVisibleItemIndex == 0 && listNow.firstVisibleItemScrollOffset == 0
    fun allowed() = enabledNow && !refreshingNow

    val connection = remember(gesture) {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                return Offset(0f, gesture.pull(available.y, atTop(), allowed()))
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (!allowed()) {
                    gesture.reset()
                    return Offset.Zero
                }
                return Offset(0f, gesture.retract(available.y))
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val hadPull = gesture.offset > 0f
                val trigger = gesture.release(allowed(), atTop())
                if (trigger) refreshNow()
                // Never steal horizontal or ordinary list-fling velocity.
                return if (hadPull) Velocity(0f, available.y) else Velocity.Zero
            }
        }
    }

    LaunchedEffect(isRefreshing, enabled) { gesture.reset() }
    val offset = if (isRefreshing) thresholdPx else if (enabled) gesture.offset else 0f
    Box(modifier.semantics {
        if (enabled && !isRefreshing) {
            customActions = listOf(CustomAccessibilityAction(refreshDescription) {
                if (gesture.request(allowed())) {
                    refreshNow()
                    true
                } else false
            })
        }
    }) {
        content(Modifier.nestedScroll(connection).graphicsLayer { translationY = offset })
        if (isRefreshing || offset > 0f) {
            val progress = (offset / thresholdPx).coerceIn(0f, 1f)
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = CpDimens.spacing2)
                    .graphicsLayer { alpha = if (isRefreshing) 1f else progress }
                    .clearAndSetSemantics {
                        contentDescription = if (isRefreshing) loadingDescription else refreshDescription
                        progressBarRangeInfo = if (isRefreshing) ProgressBarRangeInfo.Indeterminate
                            else ProgressBarRangeInfo(progress, 0f..1f)
                    },
                contentAlignment = Alignment.TopCenter,
            ) {
                CoffeePeekLoader(loadingDescription, size = CpDimens.loaderButton, strokeWidth = 2.dp)
            }
        }
    }
}
