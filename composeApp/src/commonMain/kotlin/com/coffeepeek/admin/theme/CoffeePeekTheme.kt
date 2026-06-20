package com.coffeepeek.admin.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary              = CpColor.Primary,
    onPrimary            = CpColor.DarkTextOnPrimary,
    primaryContainer     = CpColor.PrimaryDark,
    onPrimaryContainer   = CpColor.DarkTextOnPrimary,
    secondary            = CpColor.GoldWarm,
    onSecondary          = CpColor.DarkTextOnPrimary,
    secondaryContainer   = CpColor.GoldWarmHover,
    onSecondaryContainer = CpColor.DarkTextPrimary,
    background           = CpColor.DarkBackground,
    onBackground         = CpColor.DarkTextPrimary,
    surface              = CpColor.DarkSurface,
    onSurface            = CpColor.DarkTextPrimary,
    surfaceVariant       = CpColor.DarkBorder,
    onSurfaceVariant     = CpColor.DarkTextSecondary,
    outline              = CpColor.DarkBorder,
    outlineVariant       = CpColor.DarkBorderSubtle,
    error                = CpColor.Error,
    onError              = Color.White,
    scrim                = CpColor.DarkOverlay,
)

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary              = CpColor.Primary,
    onPrimary            = CpColor.LightTextOnPrimary,
    primaryContainer     = CpColor.PrimaryLight,
    onPrimaryContainer   = CpColor.LightTextOnPrimary,
    secondary            = CpColor.GoldWarm,
    onSecondary          = CpColor.LightTextOnPrimary,
    secondaryContainer   = CpColor.GoldWarmSoft,
    onSecondaryContainer = CpColor.LightTextPrimary,
    background           = CpColor.LightBackground,
    onBackground         = CpColor.LightTextPrimary,
    surface              = CpColor.LightSurface,
    onSurface            = CpColor.LightTextPrimary,
    surfaceVariant       = CpColor.LightBadge,
    onSurfaceVariant     = CpColor.LightTextSecondary,
    outline              = CpColor.LightBorder,
    outlineVariant       = CpColor.LightBorderSubtle,
    error                = CpColor.Error,
    onError              = Color.White,
    scrim                = CpColor.LightOverlay,
)

@Composable
fun CoffeePeekTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = cpTypography(),
        content     = content,
    )
}
