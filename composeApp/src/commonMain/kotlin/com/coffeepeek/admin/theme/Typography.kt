package com.coffeepeek.admin.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.manrope_bold
import coffeepeek.composeapp.generated.resources.manrope_extrabold
import coffeepeek.composeapp.generated.resources.manrope_light
import coffeepeek.composeapp.generated.resources.manrope_medium
import coffeepeek.composeapp.generated.resources.manrope_regular
import coffeepeek.composeapp.generated.resources.manrope_semibold
import org.jetbrains.compose.resources.Font

val Manrope: FontFamily
    @androidx.compose.runtime.Composable
    get() = FontFamily(
        Font(Res.font.manrope_light,     FontWeight.Light),
        Font(Res.font.manrope_regular,   FontWeight.Normal),
        Font(Res.font.manrope_medium,    FontWeight.Medium),
        Font(Res.font.manrope_semibold,  FontWeight.SemiBold),
        Font(Res.font.manrope_bold,      FontWeight.Bold),
        Font(Res.font.manrope_extrabold, FontWeight.ExtraBold),
    )

@androidx.compose.runtime.Composable
fun cpTypography(): Typography {
    val manrope = Manrope
    return Typography(
        displayLarge = TextStyle(
            fontFamily = manrope,
            fontSize = 88.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.sp,
            lineHeight = 84.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = manrope,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = manrope,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = manrope,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = manrope,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = manrope,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = manrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = manrope,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            lineHeight = (16 * 1.5).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = manrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            lineHeight = (14 * 1.5).sp,
        ),
        bodySmall = TextStyle(
            fontFamily = manrope,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = manrope,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = manrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = manrope,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
        ),
    )
}
