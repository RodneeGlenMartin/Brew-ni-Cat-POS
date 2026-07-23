package com.example.cattasticpos.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.cattasticpos.domain.model.AppThemeAccent

fun lightColorSchemeFor(accent: AppThemeAccent) = lightColorScheme(
    primary = accent.primary(darkTheme = false),
    onPrimary = accent.onPrimary,
    primaryContainer = accent.primaryContainer,
    onPrimaryContainer = accent.onPrimaryContainer,
    secondary = accent.secondary(darkTheme = false),
    onSecondary = accent.onSecondary(darkTheme = false),
    secondaryContainer = accent.secondary(darkTheme = false).copy(alpha = 0.16f),
    onSecondaryContainer = accent.onSecondary(darkTheme = false),
    tertiary = accent.tertiary(darkTheme = false),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = accent.tertiary(darkTheme = false).copy(alpha = 0.16f),
    onTertiaryContainer = accent.onPrimaryContainer,
    background = AlabasterPalette.Canvas,
    onBackground = AlabasterPalette.Heading,
    surface = Color.White.copy(alpha = 0.72f),
    onSurface = AlabasterPalette.Heading,
    surfaceVariant = Color.White.copy(alpha = 0.40f),
    onSurfaceVariant = AlabasterPalette.BodyMuted,
    outline = AlabasterPalette.RingBorder,
    outlineVariant = Color.Black.copy(alpha = 0.05f),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF)
)

fun darkColorSchemeFor(accent: AppThemeAccent) = darkColorScheme(
    primary = accent.primary(darkTheme = true),
    onPrimary = accent.onPrimary,
    primaryContainer = accent.primary(darkTheme = true).copy(alpha = 0.35f),
    onPrimaryContainer = Color.White,
    secondary = accent.secondary(darkTheme = true),
    onSecondary = accent.onSecondary(darkTheme = true),
    secondaryContainer = accent.secondary(darkTheme = true).copy(alpha = 0.22f),
    onSecondaryContainer = Color.White,
    tertiary = accent.tertiary(darkTheme = true),
    onTertiary = Color.White,
    tertiaryContainer = accent.tertiary(darkTheme = true).copy(alpha = 0.22f),
    onTertiaryContainer = Color.White,
    background = ObsidianPalette.Canvas,
    onBackground = Color.White,
    surface = ObsidianPalette.CanvasAlt,
    onSurface = Color.White,
    surfaceVariant = ObsidianPalette.GlassFill,
    onSurfaceVariant = ObsidianPalette.BodyMuted,
    outline = Color.White.copy(alpha = 0.12f),
    outlineVariant = Color.White.copy(alpha = 0.08f),
    error = Color(0xFFFF6B6B),
    onError = Color.White
)
