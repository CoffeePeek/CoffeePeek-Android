package com.coffeepeek.admin.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens

@Composable
fun CoffeePeekLoader(
    modifier: Modifier = Modifier,
    size: Dp = CpDimens.loaderDefault,
    color: Color = CpColor.Primary,
    strokeWidth: Dp = (size / 16f).coerceAtLeast(2.dp),
    contentDescription: String = "Загрузка",
) {
    val transition = rememberInfiniteTransition(label = "coffeepeek-loader")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1600
                360f at 1600 using FastOutSlowInEasing
            },
        ),
        label = "coffeepeek-loader-spin",
    )
    val morph by transition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0f at 0 using FastOutSlowInEasing
                1f at 1250 using FastOutSlowInEasing
                2f at 2500 using FastOutSlowInEasing
                3f at 3750 using FastOutSlowInEasing
                4f at 5000
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "coffeepeek-loader-morph",
    )

    Canvas(
        modifier = modifier
            .size(size)
            .semantics {
                this.contentDescription = contentDescription
            },
    ) {
        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2f
        val ringSize = Size(this.size.width - strokePx, this.size.height - strokePx)
        val wobbleX = 1f + 0.09f * kotlin.math.sin(morph * kotlin.math.PI).toFloat()
        val wobbleY = 1f + 0.08f * kotlin.math.cos(morph * kotlin.math.PI * 0.75f).toFloat()
        val corner = ringSize.minDimension * (0.32f + 0.08f * kotlin.math.sin(morph * kotlin.math.PI * 0.5f).toFloat())

        rotate(rotation) {
            scale(wobbleX, wobbleY) {
                drawArc(
                    color = color,
                    startAngle = -92f,
                    sweepAngle = 185f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = ringSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
                drawRoundRect(
                    color = color.copy(alpha = 0.18f),
                    topLeft = Offset(inset, inset),
                    size = ringSize,
                    cornerRadius = CornerRadius(corner, corner * 0.86f),
                    style = Stroke(width = strokePx),
                )
            }
        }
    }
}
