package com.coffeepeek.admin.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.utils.OrientationObserver

object Theme {

    val adaptiveGridCells = GridCells.Adaptive(300.dp)

    val elevation = 3.dp
    val round = 3.dp
    val strokeColor = Color.Gray
    val strokeWidth = 0.5.dp

    val shape = RoundedCornerShape(round)
    val shapeTop = RoundedCornerShape(topStart = round, topEnd = round, bottomStart = 0.dp, bottomEnd = 0.dp)
    val shapeBottom = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = round, bottomEnd = round)
    val border = BorderStroke(strokeWidth, strokeColor)

    val horizontalPadding = 16.dp
    val verticalPadding = 8.dp
    val gridItemPadding = 12.dp


    val brush = Brush.verticalGradient(
        colors = listOf(Colors.lightYellowBg, Colors.lightYellowBg, Colors.lightYellowBg)
    )
//    linearGradient(
//        colors = listOf(Colors.green, Colors.greenLight),
//        start = Offset(OrientationObserver.size.value.width.toFloat() / 2, 0f),
//        end = Offset(OrientationObserver.size.value.width.toFloat() / 2, OrientationObserver.size.value.height.toFloat())
//    )

}