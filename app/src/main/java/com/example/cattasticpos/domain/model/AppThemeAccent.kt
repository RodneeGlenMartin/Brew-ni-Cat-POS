package com.example.cattasticpos.domain.model

import androidx.compose.ui.graphics.Color

enum class AppThemeAccent(
    val id: String,
    val label: String,
    val swatch: Color,
    val darkPrimary: Color,
    val darkSecondary: Color,
    val darkTertiary: Color,
    val lightPrimary: Color,
    val lightSecondary: Color,
    val lightTertiary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val onDarkSecondary: Color,
    val onLightSecondary: Color
) {
    EMERALD(
        id = "emerald",
        label = "Mint Green",
        swatch = Color(0xFF2E7D32),
        darkPrimary = Color(0xFF2E7D32),
        darkSecondary = Color(0xFFFF8A80),
        darkTertiary = Color(0xFF4CAF50),
        lightPrimary = Color(0xFF1B5E20),
        lightSecondary = Color(0xFFB71C1C),
        lightTertiary = Color(0xFF2E7D32),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC8E6C9),
        onPrimaryContainer = Color(0xFF1B5E20),
        onDarkSecondary = Color(0xFF4A1410),
        onLightSecondary = Color(0xFFFFFFFF)
    ),
    COFFEE(
        id = "coffee",
        label = "Amber Orange",
        swatch = Color(0xFFE65100),
        darkPrimary = Color(0xFFE65100),
        darkSecondary = Color(0xFF00E5FF),
        darkTertiary = Color(0xFFF57C00),
        lightPrimary = Color(0xFFD84315),
        lightSecondary = Color(0xFF006064),
        lightTertiary = Color(0xFFBF360C),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFE0B2),
        onPrimaryContainer = Color(0xFF3E2723),
        onDarkSecondary = Color(0xFF00363A),
        onLightSecondary = Color(0xFFFFFFFF)
    ),
    CHARCOAL(
        id = "charcoal",
        label = "Charcoal Black",
        swatch = Color(0xFF37474F),
        darkPrimary = Color(0xFF37474F),
        darkSecondary = Color(0xFFFF8A65),
        darkTertiary = Color(0xFF546E7A),
        lightPrimary = Color(0xFF263238),
        lightSecondary = Color(0xFFBF360C),
        lightTertiary = Color(0xFF37474F),
        onPrimary = Color(0xFFF5F1E3),
        primaryContainer = Color(0xFFCFD8DC),
        onPrimaryContainer = Color(0xFF102027),
        onDarkSecondary = Color(0xFF3E1010),
        onLightSecondary = Color(0xFFFFFFFF)
    ),
    SLATE(
        id = "slate",
        label = "Sleek Slate",
        swatch = Color(0xFF4A7A96),
        darkPrimary = Color(0xFF4A7A96),
        darkSecondary = Color(0xFFD4AF37),
        darkTertiary = Color(0xFF658A9F),
        lightPrimary = Color(0xFF2C526A),
        lightSecondary = Color(0xFF8F6B00),
        lightTertiary = Color(0xFF455A64),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB3CDD9),
        onPrimaryContainer = Color(0xFF263238),
        onDarkSecondary = Color(0xFF2B2118),
        onLightSecondary = Color(0xFFFFFFFF)
    ),
    CRIMSON(
        id = "crimson",
        label = "Crimson Rose",
        swatch = Color(0xFFC2185B),
        darkPrimary = Color(0xFFC2185B),
        darkSecondary = Color(0xFF69F0AE),
        darkTertiary = Color(0xFFD81B60),
        lightPrimary = Color(0xFF880E4F),
        lightSecondary = Color(0xFF00695C),
        lightTertiary = Color(0xFFAD1457),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF8BBD0),
        onPrimaryContainer = Color(0xFF3E0718),
        onDarkSecondary = Color(0xFF00391E),
        onLightSecondary = Color(0xFFFFFFFF)
    );

    fun primary(darkTheme: Boolean): Color = if (darkTheme) darkPrimary else lightPrimary

    fun secondary(darkTheme: Boolean): Color = if (darkTheme) darkSecondary else lightSecondary

    fun tertiary(darkTheme: Boolean): Color = if (darkTheme) darkTertiary else lightTertiary

    fun onSecondary(darkTheme: Boolean): Color = if (darkTheme) onDarkSecondary else onLightSecondary

    companion object {
        const val DEFAULT_ID = "emerald"

        fun fromId(id: String?): AppThemeAccent {
            if (id.isNullOrBlank()) return EMERALD
            return entries.find { it.id == id } ?: EMERALD
        }
    }
}
