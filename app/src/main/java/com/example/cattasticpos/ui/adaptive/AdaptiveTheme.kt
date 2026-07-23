package com.example.cattasticpos.ui.adaptive

import android.content.res.Configuration
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
    // Modest density bump so the UI isn't tiny on the high-density tablet panel — but keyed to
    // orientation, not the live width. The old logic used screenWidthDp (the LONG edge), so in
    // landscape it jumped to 1.20x and rendered oversized/cramped vertically. Landscape is
    // height-constrained, so leave it at the panel's natural density; only enlarge in portrait,
    // sized off the orientation-independent smallest width.
    val configuration = LocalConfiguration.current
    val baseDensity = LocalDensity.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val smallestWidthDp = configuration.smallestScreenWidthDp
    // Owner asked for a tighter UI on the tablet (it read too large, especially landscape).
    // Only tablets (smallestWidth >= 600dp) are touched; phones stay at natural density.
    val uiScale = when {
        smallestWidthDp >= 720 -> if (isLandscape) 0.88f else 1.0f
        smallestWidthDp >= 600 -> if (isLandscape) 0.90f else 0.95f
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
