package com.coffeepeek.admin.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

object SizeExt {

    private var density = 1f
    private var fontScale = 1f

    val Dp.px get() = value * density

    val Dp.sp get() = (value / fontScale).sp

    val TextUnit.px get() = Dp(value * fontScale).px

    val Number.pxToDp get() = (this.toFloat() / density).dp

    @Composable
    operator fun invoke() {
        density = LocalDensity.current.density
        fontScale = LocalDensity.current.fontScale
    }

}