package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.utils.SizeObserver.toDP

object Insets {


    @Composable
    fun BottomInset(
        color: Color = CpColor.Primary
    ) {
        Box(
            modifier = Modifier
                .background(color)
                .padding(WindowInsets.navigationBars.asPaddingValues())
        )
    }

    @Composable
    fun TopInset(
        color: Color = CpColor.Primary
    ) {
        Box(
            modifier = Modifier
                .background(color)
                .padding(WindowInsets.statusBars.asPaddingValues())
        )
    }

    @Composable
    fun LeftInset(
        color: Color = CpColor.Primary
    ) {
        val left = WindowInsets.safeContent.getLeft(LocalDensity.current, LayoutDirection.Ltr)
        Box(
            modifier = Modifier
                .background(color = color)
                .padding(horizontal = left.toDP())
        )
    }

    @Composable
    fun RightInset(
        color: Color = CpColor.Primary
    ) {
        val right = WindowInsets.safeContent.getRight(LocalDensity.current, LayoutDirection.Ltr)
        Box(
            modifier = Modifier
                .background(color = color)
                .padding(horizontal = right.toDP())
        )
    }

    @Composable
    fun horizontalPadding(): PaddingValues {
        val paddings = WindowInsets.safeContent.asPaddingValues()
        return PaddingValues(
            start = paddings.calculateLeftPadding(LayoutDirection.Ltr),
            end = paddings.calculateRightPadding(LayoutDirection.Ltr)
        )
    }


}