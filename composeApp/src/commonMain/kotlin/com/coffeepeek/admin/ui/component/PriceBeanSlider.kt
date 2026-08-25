package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.coffeepeek.admin.theme.CpColor
import com.coffeepeek.admin.theme.CpDimens
import com.coffeepeek.admin.ui.icons.CpIcons
import kotlin.math.roundToInt

/** Parses domain price labels (`$`…`$$$$`) into level 1–4. */
fun priceRangeLevel(priceRange: String?): Int? {
    val level = priceRange?.count { it == '$' } ?: return null
    return level.takeIf { it in 1..4 }
}

@Composable
fun PriceBeansRow(
    level: Int,
    modifier: Modifier = Modifier,
    maxLevel: Int = 4,
    iconSize: Dp = 14.dp,
    activeTint: Color = CpColor.Primary,
    inactiveTint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    showEmptySlots: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(2.dp),
) {
    val filled = level.coerceIn(0, maxLevel)
    val count = if (showEmptySlots) maxLevel else filled
    if (count <= 0) return
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            Icon(
                imageVector = CpIcons.CoffeeBean,
                contentDescription = null,
                tint = if (index < filled) activeTint else inactiveTint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

/**
 * Discrete price filter: 0 = any, 1–4 = coffee-bean levels.
 * Each bean is aligned above the corresponding slider tick (1..4).
 */
@Composable
fun PriceBeanSlider(
    selected: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Цена",
) {
    val value = (selected ?: 0).coerceIn(0, 4).toFloat()
    val filled = (selected ?: 0).coerceIn(0, 4)
    val iconSize = 22.dp
    // Match Material3 thumb travel inset so beans sit over tick centers.
    val trackInset = 10.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CpDimens.spacing2),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = priceLevelLabel(selected),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Four equal segments cover ticks 0→1, 1→2, 2→3, 3→4.
        // Place each bean so its center sits on ticks 1, 2, 3, 4.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = trackInset)
                .height(iconSize),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(
                        imageVector = CpIcons.CoffeeBean,
                        contentDescription = null,
                        tint = if (index < filled) {
                            CpColor.Primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier
                            .size(iconSize)
                            .offset(x = iconSize / 2),
                    )
                }
            }
        }

        Slider(
            value = value,
            onValueChange = { raw ->
                val next = raw.roundToInt().coerceIn(0, 4)
                onSelect(next.takeIf { it > 0 })
            },
            valueRange = 0f..4f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = CpColor.Primary,
                activeTrackColor = CpColor.Primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            ),
        )
    }
}

private fun priceLevelLabel(selected: Int?): String = when (selected) {
    null, 0 -> "Любая"
    1 -> "1 зерно"
    in 2..4 -> "$selected зерна"
    else -> "Любая"
}
