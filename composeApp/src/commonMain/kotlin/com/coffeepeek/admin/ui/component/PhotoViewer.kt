package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

expect object PhotoViewer {


    @Composable
    operator fun invoke(
        images: List<String>,
        isEdit: Boolean = false,
        onAdd: () -> Unit = {},
        onDelete: (String) -> Unit = {},
        preferredItemWidth: Dp = 400.dp,
        itemSpacing: Dp = 8.dp,
        cardModifier: Modifier,
        modifier: Modifier,
    )

}