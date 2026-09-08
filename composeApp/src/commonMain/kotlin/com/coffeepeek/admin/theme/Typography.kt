package com.coffeepeek.admin.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.rf_dewi_condensed_regular
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_black
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_bold
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_light
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_regular
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_semibold
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_ultrabold
import coffeepeek.composeapp.generated.resources.rf_dewi_expanded_ultralight
import coffeepeek.composeapp.generated.resources.rf_dewi_extended_regular
import coffeepeek.composeapp.generated.resources.rf_dewi_regular
import org.jetbrains.compose.resources.Font

val RfDewi: FontFamily
    @androidx.compose.runtime.Composable
    get() = FontFamily(
        Font(Res.font.rf_dewi_regular, FontWeight.Normal),
    )

val RfDewiCondensed: FontFamily
    @androidx.compose.runtime.Composable
    get() = FontFamily(
        Font(Res.font.rf_dewi_condensed_regular, FontWeight.Normal),
    )

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

val RfDewiExtended: FontFamily
    @androidx.compose.runtime.Composable
    get() = FontFamily(
        Font(Res.font.rf_dewi_extended_regular, FontWeight.Normal),
    )

@androidx.compose.runtime.Composable
fun cpTypography(): Typography {
    val regular = RfDewi
    val condensed = RfDewiCondensed
    val expanded = RfDewiExpanded
    val extended = RfDewiExtended
    return Typography(
        displayLarge = TextStyle(
            fontFamily = expanded,
            fontSize = 88.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            lineHeight = 84.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = extended,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = extended,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = condensed,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = condensed,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = condensed,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = condensed,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = regular,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            lineHeight = (16 * 1.5).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = regular,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            lineHeight = (14 * 1.5).sp,
        ),
        bodySmall = TextStyle(
            fontFamily = regular,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = regular,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = regular,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = regular,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
        ),
    )
}
