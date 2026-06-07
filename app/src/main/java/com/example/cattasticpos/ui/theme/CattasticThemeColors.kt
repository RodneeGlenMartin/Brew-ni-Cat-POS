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
    secondaryContainer = Color(0xFFD7CCC8),
    onSecondaryContainer = Color(0xFF3B2821),
    tertiary = Color(0xFF42A5F5),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F1E3),
    onBackground = Color(0xFF3B2821),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF3B2821),
    surfaceVariant = Color(0xFFF5EFEA),
    onSurfaceVariant = Color(0xFF3B2821),
    outline = Color(0xFF80756C),
    outlineVariant = Color(0xFFD2C4B9),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF)
)

fun darkColorSchemeFor(accent: AppThemeAccent) = darkColorScheme(
    primary = accent.primary,
    onPrimary = Color.White,
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
