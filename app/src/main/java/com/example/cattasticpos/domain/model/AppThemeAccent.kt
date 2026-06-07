package com.example.cattasticpos.domain.model

import androidx.compose.ui.graphics.Color

enum class AppThemeAccent(
    val id: String,
    val label: String,
    val swatch: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color
) {
    EMERALD(
        id = "emerald",
        label = "Deep Emerald Green",
        swatch = Color(0xFF3E5C49),
        primary = Color(0xFF3E5C49),
        onPrimary = Color(0xFFF5F1E3),
        primaryContainer = Color(0xFFD3A63B),
        onPrimaryContainer = Color(0xFF3B2821),
        secondary = Color(0xFFE08538)
    ),
    COFFEE(
        id = "coffee",
        label = "Dark Coffee Brown",
        swatch = Color(0xFF4E342E),
        primary = Color(0xFF4E342E),
        onPrimary = Color(0xFFF5F1E3),
        primaryContainer = Color(0xFFBCAAA4),
        onPrimaryContainer = Color(0xFF2B1A12),
        secondary = Color(0xFF8D6E63)
    ),
    CHARCOAL(
        id = "charcoal",
        label = "Charcoal Black",
        swatch = Color(0xFF37474F),
        primary = Color(0xFF37474F),
        onPrimary = Color(0xFFF5F1E3),
        primaryContainer = Color(0xFF78909C),
        onPrimaryContainer = Color(0xFF102027),
        secondary = Color(0xFF546E7A)
    ),
    SLATE(
        id = "slate",
        label = "Sleek Slate",
        swatch = Color(0xFF546E7A),
        primary = Color(0xFF546E7A),
        onPrimary = Color(0xFFF5F1E3),
        primaryContainer = Color(0xFFB0BEC5),
        onPrimaryContainer = Color(0xFF263238),
        secondary = Color(0xFF607D8B)
    ),
    CRIMSON(
        id = "crimson",
        label = "Crimson Velvet",
        swatch = Color(0xFF8B1538),
        primary = Color(0xFF8B1538),
        onPrimary = Color(0xFFFFF8F8),
        primaryContainer = Color(0xFFD4A5A5),
        onPrimaryContainer = Color(0xFF3E0718),
        secondary = Color(0xFFB71C4A)
    );

    companion object {
        const val DEFAULT_ID = "emerald"

        fun fromId(id: String?): AppThemeAccent {
            if (id.isNullOrBlank()) return EMERALD
            return entries.find { it.id == id } ?: EMERALD
        }
    }
}
