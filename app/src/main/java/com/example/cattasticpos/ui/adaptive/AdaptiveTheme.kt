package com.example.cattasticpos.ui.adaptive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.cattasticpos.domain.model.AppThemeAccent
import com.example.cattasticpos.ui.theme.darkColorSchemeFor
import com.example.cattasticpos.ui.theme.lightColorSchemeFor

data class CupertinoColors(
    val accent: Color,
    val onAccent: Color,
    val label: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val systemBackground: Color,
    val secondarySystemBackground: Color,
    val groupedBackground: Color,
    val separator: Color,
    val fill: Color
)

val LocalCupertinoColors = staticCompositionLocalOf {
    CupertinoColors(
        accent = Color(0xFF007AFF),
        onAccent = Color.White,
        label = Color.Black,
        secondaryLabel = Color(0x993C3C43),
        tertiaryLabel = Color(0x4D3C3C43),
        systemBackground = Color(0xFFF2F2F7),
        secondarySystemBackground = Color.White,
        groupedBackground = Color.White,
        separator = Color(0xFFC6C6C8),
        fill = Color(0x78788033)
    )
}

fun cupertinoColorsFor(accent: AppThemeAccent, darkTheme: Boolean): CupertinoColors {
    val material = if (darkTheme) darkColorSchemeFor(accent) else lightColorSchemeFor(accent)
    return CupertinoColors(
        accent = material.primary,
        onAccent = material.onPrimary,
        label = material.onBackground,
        secondaryLabel = material.onSurfaceVariant.copy(alpha = 0.72f),
        tertiaryLabel = material.onSurfaceVariant.copy(alpha = 0.45f),
        systemBackground = material.background,
        secondarySystemBackground = material.surface,
        groupedBackground = material.surfaceVariant.copy(alpha = if (darkTheme) 0.55f else 1f),
        separator = material.outlineVariant,
        fill = material.outline.copy(alpha = 0.25f)
    )
}

@Composable
fun CupertinoTheme(
    accent: AppThemeAccent,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = cupertinoColorsFor(accent, darkTheme)
    CompositionLocalProvider(LocalCupertinoColors provides colors) {
        content()
    }
}

enum class AdaptiveThemeTarget {
    Material,
    Cupertino
}

@Composable
fun AdaptiveTheme(
    accent: AppThemeAccent = AppThemeAccent.EMERALD,
    darkTheme: Boolean = isSystemInDarkTheme(),
    target: AdaptiveThemeTarget = AdaptiveThemeTarget.Cupertino,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorSchemeFor(accent) else lightColorSchemeFor(accent)
    MaterialTheme(colorScheme = colorScheme) {
        CupertinoTheme(accent = accent, darkTheme = darkTheme) {
            content()
        }
    }
}
