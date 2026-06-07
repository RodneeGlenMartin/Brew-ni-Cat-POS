package com.example.cattasticpos.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.cattasticpos.domain.model.AppThemeAccent

fun lightColorSchemeFor(accent: AppThemeAccent) = lightColorScheme(
    primary = accent.primary,
    onPrimary = accent.onPrimary,
    primaryContainer = accent.primaryContainer,
    onPrimaryContainer = accent.onPrimaryContainer,
    secondary = accent.secondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = accent.primaryContainer.copy(alpha = 0.35f),
    onSecondaryContainer = Color(0xFF2B2118),
    tertiary = Color(0xFF42A5F5),
    onTertiary = Color(0xFFFFFFFF),
    background = AlabasterPalette.Canvas,
    onBackground = AlabasterPalette.Heading,
    surface = AlabasterPalette.CanvasAlt,
    onSurface = AlabasterPalette.Heading,
    surfaceVariant = AlabasterPalette.GlassFill,
    onSurfaceVariant = AlabasterPalette.BodyMuted,
    outline = AlabasterPalette.RingBorder,
    outlineVariant = Color.Black.copy(alpha = 0.04f),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF)
)

fun darkColorSchemeFor(accent: AppThemeAccent) = darkColorScheme(
    primary = accent.primary,
    onPrimary = accent.onPrimary,
    primaryContainer = accent.primary.copy(alpha = 0.35f),
    onPrimaryContainer = Color.White,
    secondary = accent.secondary,
    onSecondary = Color.White,
    secondaryContainer = accent.secondary.copy(alpha = 0.25f),
    onSecondaryContainer = ObsidianPalette.BodyMuted,
    tertiary = accent.primaryContainer,
    onTertiary = Color.White,
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
