package com.coffeepeek.admin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.coffeepeek.admin.theme.CpDimens

data class SettingsIconColors(
    val background: Color,
    val icon: Color,
)

object SettingsIconPalette {
    val Mint = SettingsIconColors(
        background = Color(0xFFECFAED),
        icon = Color(0xFF5CB66A),
    )
    val Cyan = SettingsIconColors(
        background = Color(0xFFD3EFFA),
        icon = Color(0xFF42AFC2),
    )
    val Sky = SettingsIconColors(
        background = Color(0xFFECF6FF),
        icon = Color(0xFF4AA9EE),
    )
    val Aqua = SettingsIconColors(
        background = Color(0xFFDDFAF8),
        icon = Color(0xFF4DBDB8),
    )
    val Lavender = SettingsIconColors(
        background = Color(0xFFFFF1FF),
        icon = Color(0xFFC96DDD),
    )
    val Gold = SettingsIconColors(
        background = Color(0xFFFEF3C7),
        icon = Color(0xFFCA8A04),
    )
    val Emerald = SettingsIconColors(
        background = Color(0xFFECFDF5),
        icon = Color(0xFF10B981),
    )
    val Blue = SettingsIconColors(
        background = Color(0xFFEFF6FF),
        icon = Color(0xFF3B82F6),
    )
    val Rose = SettingsIconColors(
        background = Color(0xFFFFF1F2),
        icon = Color(0xFFF43F5E),
    )
    val Violet = SettingsIconColors(
        background = Color(0xFFF5F3FF),
        icon = Color(0xFF8B5CF6),
    )
    val BrightCyan = SettingsIconColors(
        background = Color(0xFFECFEFF),
        icon = Color(0xFF06B6D4),
    )
}

@Composable
fun SettingsIconBadge(
    icon: ImageVector,
    colors: SettingsIconColors,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(CpDimens.settingsIconContainer)
            .clip(RoundedCornerShape(CpDimens.settingsIconRadius))
            .background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.icon,
            modifier = Modifier.size(CpDimens.settingsIconSize),
        )
    }
}
