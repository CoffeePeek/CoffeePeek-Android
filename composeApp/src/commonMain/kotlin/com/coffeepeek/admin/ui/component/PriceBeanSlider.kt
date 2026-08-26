package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.DpSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.ic_byn_symbol
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource

private const val MAX_PRICE_LEVEL = 3
private const val BYN_ICON_ASPECT = 360.67f / 446.4f
private const val BYN_ICON_SCALE = 0.9f

private fun Modifier.bynIconSize(width: Dp): Modifier {
    val scaledWidth = width * BYN_ICON_SCALE
    return size(DpSize(scaledWidth, scaledWidth / BYN_ICON_ASPECT))
}

/** Parses domain price labels (`$`…`$$$$`) into level 1–3 for UI. */
fun priceRangeLevel(priceRange: String?): Int? {
    val level = priceRange?.count { it == '$' } ?: return null
    return level.coerceIn(1, 4).coerceAtMost(MAX_PRICE_LEVEL)
}

@Composable
fun PriceBynIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    contentDescription: String? = null,
) {
    Icon(
        painter = painterResource(Res.drawable.ic_byn_symbol),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier,
    )
}

@Composable
fun PriceBynRow(
    level: Int,
    modifier: Modifier = Modifier,
    maxLevel: Int = MAX_PRICE_LEVEL,
    iconSize: Dp = 14.dp,
    activeTint: Color = CpColor.Primary,
    inactiveTint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(1.dp),
) {
    val filled = level.coerceIn(0, maxLevel)
    if (filled <= 0) return
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(filled) {
            PriceBynIcon(
                modifier = Modifier.bynIconSize(iconSize),
                tint = activeTint,
            )
        }
    }
}

/** @deprecated Use [PriceBynRow]. */
@Composable
fun PriceBeansRow(
    level: Int,
    modifier: Modifier = Modifier,
    maxLevel: Int = MAX_PRICE_LEVEL,
    iconSize: Dp = 14.dp,
    activeTint: Color = CpColor.Primary,
    inactiveTint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    showEmptySlots: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(2.dp),
) {
    PriceBynRow(
        level = level,
        modifier = modifier,
        maxLevel = maxLevel,
        iconSize = iconSize,
        activeTint = activeTint,
        inactiveTint = inactiveTint,
        horizontalArrangement = horizontalArrangement,
    )
}

/**
 * Discrete price filter: null/0 = any, 1–3 = BYN tiers (как в макете).
 */
@Composable
fun PriceBeanSlider(
    selected: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Цена",
    showTitle: Boolean = true,
) {
    val value = (selected ?: 0).coerceIn(0, MAX_PRICE_LEVEL).toFloat()
    val filled = (selected ?: 0).coerceIn(0, MAX_PRICE_LEVEL)
    val iconWidth = 18.dp
    val hint = priceLevelHint(selected)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing1),
    ) {
        if (showTitle) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (filled > 0) {
                    Text(
                        text = " $filled",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = CpColor.Primary,
                    )
                }
            }
        }

        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = CpColor.Primary,
            )
        }

        Slider(
            value = value,
            onValueChange = { raw ->
                val next = raw.roundToInt().coerceIn(0, MAX_PRICE_LEVEL)
                onSelect(next.takeIf { it > 0 })
            },
            valueRange = 0f..MAX_PRICE_LEVEL.toFloat(),
            steps = MAX_PRICE_LEVEL - 1,
            colors = SliderDefaults.colors(
                thumbColor = CpColor.Primary,
                activeTrackColor = CpColor.Primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(MAX_PRICE_LEVEL) { index ->
                val tier = index + 1
                val isActive = filled == tier
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(tier) {
                        PriceBynIcon(
                            modifier = Modifier.bynIconSize(iconWidth),
                            tint = if (isActive) {
                                CpColor.Primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun priceLevelHint(selected: Int?): String? = when (selected) {
    1 -> "Капучино < 8"
    2 -> "Капучино ≈ 8"
    3 -> "Капучино > 8"
    else -> null
}
