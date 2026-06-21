package com.example.cattasticpos.ui.adaptive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.cattasticpos.domain.model.AppThemeAccent
import com.example.cattasticpos.ui.theme.AlabasterPalette
import com.example.cattasticpos.ui.theme.ObsidianPalette
import com.example.cattasticpos.ui.theme.adaptiveTypography
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
    return if (darkTheme) {
        CupertinoColors(
            accent = material.primary,
            onAccent = Color.White,
            label = Color.White,
            secondaryLabel = ObsidianPalette.BodyMuted,
            tertiaryLabel = Color.White.copy(alpha = 0.45f),
            systemBackground = material.background,
            secondarySystemBackground = material.surface,
            groupedBackground = ObsidianPalette.GlassFill,
            separator = Color.White.copy(alpha = 0.1f),
            fill = Color.White.copy(alpha = 0.06f)
        )
    } else {
        CupertinoColors(
            accent = material.primary,
            onAccent = material.onPrimary,
            label = AlabasterPalette.Heading,
            secondaryLabel = AlabasterPalette.BodyMuted,
            tertiaryLabel = Color.Black.copy(alpha = 0.45f),
            systemBackground = material.background,
            secondarySystemBackground = material.surface,
            groupedBackground = AlabasterPalette.GlassFill,
            separator = AlabasterPalette.RingBorder,
            fill = Color.Black.copy(alpha = 0.04f)
        )
    }
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
    // On large tablets (e.g. Redmi Pad 2 in landscape) the UI otherwise renders small on the
    // high-density panel, so scale text and spacing up a notch for comfortable reading/tapping.
    val configuration = LocalConfiguration.current
    val baseDensity = LocalDensity.current
    val uiScale = when {
        configuration.screenWidthDp >= 1000 -> 1.20f
        configuration.screenWidthDp >= 720 -> 1.12f
        else -> 1f
    }
    val scaledDensity = Density(
        density = baseDensity.density * uiScale,
        fontScale = baseDensity.fontScale
    )
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = adaptiveTypography(darkTheme)
        ) {
            CupertinoTheme(accent = accent, darkTheme = darkTheme) {
                content()
            }
        }
    }
}
