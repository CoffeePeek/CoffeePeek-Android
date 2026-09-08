package com.coffeepeek.admin.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coffeepeek.composeapp.generated.resources.Res
import coffeepeek.composeapp.generated.resources.inter_black
import coffeepeek.composeapp.generated.resources.inter_bold
import coffeepeek.composeapp.generated.resources.inter_extrabold
import coffeepeek.composeapp.generated.resources.inter_light
import coffeepeek.composeapp.generated.resources.inter_medium
import coffeepeek.composeapp.generated.resources.inter_regular
import coffeepeek.composeapp.generated.resources.inter_semibold
import org.jetbrains.compose.resources.Font

val Inter: FontFamily
    @androidx.compose.runtime.Composable
    get() = FontFamily(
        Font(Res.font.inter_light,     FontWeight.Light),
        Font(Res.font.inter_regular,   FontWeight.Normal),
        Font(Res.font.inter_medium,    FontWeight.Medium),
        Font(Res.font.inter_semibold,  FontWeight.SemiBold),
        Font(Res.font.inter_bold,      FontWeight.Bold),
        Font(Res.font.inter_extrabold, FontWeight.ExtraBold),
        Font(Res.font.inter_black,     FontWeight.Black),
    )

@androidx.compose.runtime.Composable
fun cpTypography(): Typography {
    val inter = Inter
    return Typography(
        displayLarge = TextStyle(
            fontFamily = inter,
            fontSize = 88.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            lineHeight = 84.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = inter,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = inter,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = inter,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = inter,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = inter,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = inter,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            lineHeight = (16 * 1.5).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            lineHeight = (14 * 1.5).sp,
        ),
        bodySmall = TextStyle(
            fontFamily = inter,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = inter,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = inter,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
        ),
    )
}
