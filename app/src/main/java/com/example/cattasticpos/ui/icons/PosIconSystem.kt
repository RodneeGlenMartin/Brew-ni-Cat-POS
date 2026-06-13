package com.example.cattasticpos.ui.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cattasticpos.ui.theme.adaptiveBodyMuted
import com.example.cattasticpos.ui.theme.adaptiveGlassBrush
import com.example.cattasticpos.ui.theme.neonSelectionBrush

/**
 * Icon sizing tokens. Gradient is default via [FluentIcon]; flat accent tint for toolbar chrome only.
 */
object PosIconSize {
    val Compact = 14.dp
    val Small = 16.dp
    val Medium = 20.dp
    val Default = 24.dp
    val Hero = 32.dp
}

@Composable
fun PosIconBadge(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = PosIconSize.Medium,
    emphasized: Boolean = false,
    error: Boolean = false,
    useGlassGradient: Boolean = true,
    flatTint: Color? = null
) {
    val darkTheme = isSystemInDarkTheme()
    val shape = RoundedCornerShape(12.dp)
    val accent = MaterialTheme.colorScheme.primary
    val borderBrush = when {
        error -> androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
        emphasized -> neonSelectionBrush(accent)
        else -> androidx.compose.ui.graphics.SolidColor(
            if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
        )
    }
    val fill = when {
        error -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        emphasized -> Color.Transparent
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }
    val backgroundModifier = if (emphasized) {
        Modifier.background(adaptiveGlassBrush(darkTheme))
    } else {
        Modifier.background(fill)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(backgroundModifier)
            .border(1.dp, borderBrush, shape),
        contentAlignment = Alignment.Center
    ) {
        FluentIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            size = iconSize,
            tint = flatTint,
            useGlassGradient = useGlassGradient && flatTint == null
        )
    }
}

@Composable
fun PosChipIcon(
    imageVector: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val darkTheme = isSystemInDarkTheme()
    FluentIcon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = modifier,
        size = PosIconSize.Small,
        tint = if (selected) null else adaptiveBodyMuted(darkTheme),
        useGlassGradient = selected
    )
}
