package com.coffeepeek.core.designsystem

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coffeepeek.core.designsystem.icons.CpIcons
import com.coffeepeek.core.designsystem.theme.CpColor
import com.coffeepeek.core.designsystem.theme.CpDimens
import com.coffeepeek.core.designsystem.theme.cpTypography
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DesignTokensTest {
    @Test fun sharedDimensionsKeepLegacyValues() {
        assertEquals(44.dp, CpDimens.controlHeight)
        assertEquals(22.dp, CpDimens.buttonRadius)
        assertEquals(16.dp, CpDimens.radiusLg)
        assertEquals(24.dp, CpDimens.cardPadding)
    }

    @Test fun typographyUsesSuppliedFontInsteadOfApplicationResources() {
        val typography = cpTypography(FontFamily.Monospace)
        assertEquals(FontFamily.Monospace, typography.bodyLarge.fontFamily)
        assertEquals(16.sp, typography.bodyLarge.fontSize)
        assertEquals(24.sp, typography.bodyLarge.lineHeight)
        assertEquals(FontFamily.Monospace, typography.labelLarge.fontFamily)
    }

    @Test fun iconsKeepIntentionalAliases() {
        assertSame(CpIcons.Star, CpIcons.StarOutline)
        assertSame(CpIcons.Location, CpIcons.MyLocation)
        assertTrue(CpIcons.Close.defaultWidth.value > 0)
    }

    @Test fun paletteKeepsBrandColor() {
        assertEquals(0xFFEAB308.toInt(), CpColor.Primary.toArgb())
    }
}
