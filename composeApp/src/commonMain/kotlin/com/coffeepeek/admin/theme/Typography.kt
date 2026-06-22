package com.coffeepeek.admin.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_black
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_bold
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_light
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_regular
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_semibold
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_ultrabold
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_ultralight
import org.jetbrains.compose.resources.Font

val RfDewiExpanded: FontFamily
    @androidx.compose.runtime.Composable
    get() = FontFamily(
        Font(Res.font.rf_dewi_expanded_ultralight, FontWeight.ExtraLight),
        Font(Res.font.rf_dewi_expanded_light,      FontWeight.Light),
        Font(Res.font.rf_dewi_expanded_regular,    FontWeight.Normal),
        Font(Res.font.rf_dewi_expanded_semibold,   FontWeight.SemiBold),
        Font(Res.font.rf_dewi_expanded_bold,       FontWeight.Bold),
        Font(Res.font.rf_dewi_expanded_ultrabold,  FontWeight.ExtraBold),
        Font(Res.font.rf_dewi_expanded_black,      FontWeight.Black),
    )

@androidx.compose.runtime.Composable
fun cpTypography(): Typography {
    val display = RfDewiExpanded
    return Typography(
        displayLarge = TextStyle(
            fontFamily = display,
            fontSize = 88.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-3.96).sp,
            lineHeight = 84.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = display,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.72).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = display,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = display,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        ),
        titleLarge = TextStyle(
            fontFamily = display,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        ),
        titleMedium = TextStyle(
            fontFamily = display,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        titleSmall = TextStyle(
            fontFamily = display,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        bodyLarge = TextStyle(
            fontFamily = display,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (16 * 1.5).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = display,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (14 * 1.5).sp,
        ),
        bodySmall = TextStyle(
            fontFamily = display,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
        ),
        labelLarge = TextStyle(
            fontFamily = display,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        labelMedium = TextStyle(
            fontFamily = display,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        ),
        labelSmall = TextStyle(
            fontFamily = display,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        ),
    )
}
