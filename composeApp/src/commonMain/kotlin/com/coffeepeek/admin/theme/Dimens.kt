package com.coffeepeek.admin.theme

import androidx.compose.ui.unit.dp

object CpDimens {

    // ── Spacing ───────────────────────────────────────────────────────────────
    val spacing1  =  4.dp
    val spacing2  =  8.dp
    val spacing3  = 12.dp
    val spacing4  = 16.dp
    val spacing5  = 20.dp
    val spacing6  = 24.dp
    val spacing8  = 32.dp
    val spacing10 = 40.dp
    val spacing12 = 48.dp
    val spacing16 = 64.dp

    // ── Border radius ─────────────────────────────────────────────────────────
    val radiusSm  =  8.dp
    val radiusMd  = 12.dp
    val radiusLg  = 16.dp
    val radiusXl  = 20.dp
    val radius2xl = 24.dp
    val radius3xl = 26.dp
    val radius4xl = 28.dp

    // ── Button ────────────────────────────────────────────────────────────────
    val buttonHeight    = 40.dp
    val buttonPaddingH  = 16.dp
    val buttonPaddingV  = 10.dp
    val buttonRadius    = 12.dp

    // ── Input ─────────────────────────────────────────────────────────────────
    val inputMinHeight  = 52.dp
    val inputPadding    = 16.dp
    val inputRadius     = 26.dp

    // ── Auth ──────────────────────────────────────────────────────────────────
    val authCardRadius      = 24.dp
    val authCardPadding     = 40.dp
    val authFieldHeight     = 50.dp
    val authFieldRadius     = 12.dp
    val authPrimaryBtnHeight = 48.dp
    val authMascotSize      = 128.dp
    val authThemeToggleSize = 40.dp

    // ── Select ────────────────────────────────────────────────────────────────
    val selectMinHeight = 56.dp
    val selectRadius    = 16.dp

    // ── Card ──────────────────────────────────────────────────────────────────
    val cardRadius  = 16.dp
    val cardPadding = 24.dp

    // ── Header ────────────────────────────────────────────────────────────────
    val headerHeight   = 64.dp
    val headerLogoSize = 40.dp
    val headerAvatar   = 30.dp

    // ── Toast ─────────────────────────────────────────────────────────────────
    val toastMinWidth = 320.dp
    val toastRadius   = 12.dp
    val toastPadding  = 16.dp

    // ── Loader ────────────────────────────────────────────────────────────────
    val loaderDefault = 48.dp
    val loaderButton  = 20.dp

    // ── Floating bottom nav ───────────────────────────────────────────────────
    val floatingNavBarHeight = 64.dp
    val floatingNavHorizontalMargin = 28.dp
    val floatingNavBottomMargin = 14.dp
    /** Approximate clearance: bar + bottom margin (+ caller adds nav-bar inset). */
    val floatingNavContentClearance = floatingNavBarHeight + floatingNavBottomMargin + spacing2
}
