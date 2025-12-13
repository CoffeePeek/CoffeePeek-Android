package com.coffeepeek.admin.utils

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

object Transactions {

    fun FadeIn(
        animationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
        initialAlpha: Float = 0f
    ) = fadeIn(animationSpec, initialAlpha)

    fun FadeOut(
        animationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
        targetAlpha: Float = 0f,
    ) = fadeOut(animationSpec, targetAlpha)

    fun SlideIn(
        animationSpec: FiniteAnimationSpec<IntOffset> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            ),
        initialOffset: (fullSize: IntSize) -> IntOffset,
    ) = slideIn(animationSpec, initialOffset)

    fun SlideOut(
        animationSpec: FiniteAnimationSpec<IntOffset> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            ),
        targetOffset: (fullSize: IntSize) -> IntOffset,
    ) = slideOut(animationSpec, targetOffset)

    fun ScaleIn(
        animationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
        initialScale: Float = 0f,
        transformOrigin: TransformOrigin = TransformOrigin.Center,
    ) = scaleIn(animationSpec, initialScale, transformOrigin)

    fun ScaleOut(
        animationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
        targetScale: Float = 0f,
        transformOrigin: TransformOrigin = TransformOrigin.Center
    ) = scaleOut(animationSpec, targetScale, transformOrigin)

    fun ExpandIn(
        animationSpec: FiniteAnimationSpec<IntSize> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntSize.VisibilityThreshold
            ),
        expandFrom: Alignment = Alignment.BottomEnd,
        clip: Boolean = true,
        initialSize: (fullSize: IntSize) -> IntSize = { IntSize(0, 0) },
    ) = expandIn(animationSpec, expandFrom, clip, initialSize)


    fun ShrinkOut(
        animationSpec: FiniteAnimationSpec<IntSize> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntSize.VisibilityThreshold
            ),
        shrinkTowards: Alignment = Alignment.BottomEnd,
        clip: Boolean = true,
        targetSize: (fullSize: IntSize) -> IntSize = { IntSize(0, 0) },
    ) = shrinkOut(animationSpec, shrinkTowards, clip, targetSize)


    fun ExpandHorizontally(
        animationSpec: FiniteAnimationSpec<IntSize> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntSize.VisibilityThreshold
            ),
        expandFrom: Alignment.Horizontal = Alignment.End,
        clip: Boolean = true,
        initialWidth: (fullWidth: Int) -> Int = { 0 },
    ) = expandHorizontally(animationSpec, expandFrom, clip, initialWidth)

    fun ExpandVertically(
        animationSpec: FiniteAnimationSpec<IntSize> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntSize.VisibilityThreshold
            ),
        expandFrom: Alignment.Vertical = Alignment.Bottom,
        clip: Boolean = true,
        initialHeight: (fullHeight: Int) -> Int = { 0 },
    ) = expandVertically(animationSpec, expandFrom, clip, initialHeight)


    fun ShrinkHorizontally(
        animationSpec: FiniteAnimationSpec<IntSize> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntSize.VisibilityThreshold
            ),
        shrinkTowards: Alignment.Horizontal = Alignment.End,
        clip: Boolean = true,
        targetWidth: (fullWidth: Int) -> Int = { 0 }
    ) = shrinkHorizontally(animationSpec, shrinkTowards, clip, targetWidth)

    fun ShrinkVertically(
        animationSpec: FiniteAnimationSpec<IntSize> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntSize.VisibilityThreshold
            ),
        shrinkTowards: Alignment.Vertical = Alignment.Bottom,
        clip: Boolean = true,
        targetHeight: (fullHeight: Int) -> Int = { 0 },
    ) = shrinkVertically(animationSpec, shrinkTowards, clip, targetHeight)

    fun SlideInHorizontally(
        animationSpec: FiniteAnimationSpec<IntOffset> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            ),
        initialOffsetX: (fullWidth: Int) -> Int = { -it / 2 },
    ) = slideInHorizontally(animationSpec, initialOffsetX)

    fun SlideInVerticaly(
        animationSpec: FiniteAnimationSpec<IntOffset> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            ),
        initialOffsetY: (fullHeight: Int) -> Int = { -it / 2 },
    ) = slideInVertically(animationSpec, initialOffsetY)


    fun SlideOutHorizontally(
        animationSpec: FiniteAnimationSpec<IntOffset> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            ),
        targetOffsetX: (fullWidth: Int) -> Int = { -it / 2 },
    ) = slideOutHorizontally(animationSpec, targetOffsetX)

    fun SlideOutVertically(
        animationSpec: FiniteAnimationSpec<IntOffset> =
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            ),
        targetOffsetY: (fullHeight: Int) -> Int = { -it / 2 },
    ) = slideOutVertically(animationSpec, targetOffsetY)

}