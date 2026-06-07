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
    onPrimary = accent.onPrimary,
    primaryContainer = accent.primaryContainer,
    onPrimaryContainer = accent.onPrimaryContainer,
    secondary = accent.secondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF5D4037),
    onSecondaryContainer = Color(0xFFD7CCC8),
    tertiary = Color(0xFF64B5F6),
    onTertiary = Color(0xFF003258),
    background = Color(0xFF241E1C),
    onBackground = Color(0xFFF5F1E3),
    surface = Color(0xFF302A28),
    onSurface = Color(0xFFF5F1E3),
    surfaceVariant = Color(0xFF3A3330),
    onSurfaceVariant = Color(0xFFF5F1E3),
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF424242),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)
