package com.coffeepeek.admin.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SizeObserver {


    private val _oneDp = MutableStateFlow(1f)
    val oneDp = _oneDp.asStateFlow()
    val oneDpValue get() = _oneDp.value


    fun Number.toDP(): Dp = (this.toFloat() / oneDpValue).dp

    fun Number.toPx(): Float = (this.toFloat() * oneDpValue)

    fun Dp.toPx(): Float = value.toPx()

    @Composable
    operator fun invoke() {
        Box(
            modifier = Modifier
                .size(10.dp)
                .onSizeChanged { _oneDp.value = it.width / 10f }
        )
    }


}