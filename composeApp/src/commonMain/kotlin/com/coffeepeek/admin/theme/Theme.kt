package com.coffeepeek.admin.theme

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Theme {
    val adaptiveGridCells = GridCells.Adaptive(300.dp)
    val shape = RoundedCornerShape(CpDimens.radiusSm)
    val shapeTop = RoundedCornerShape(topStart = CpDimens.radiusSm, topEnd = CpDimens.radiusSm)
    val shapeBottom = RoundedCornerShape(bottomStart = CpDimens.radiusSm, bottomEnd = CpDimens.radiusSm)
}
