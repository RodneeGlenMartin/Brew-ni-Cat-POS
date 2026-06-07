package com.example.cattasticpos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ObsidianPalette {
    val Canvas = Color(0xFF050505)
    val CanvasAlt = Color(0xFF0B0B0C)
    val GlassFill = Color.White.copy(alpha = 0.04f)
    val BodyMuted = Color.White.copy(alpha = 0.7f)
    val EmeraldGlow = Color(0xFF10B981)
    val GoldGlow = Color(0xFFD4AF37)
    val Handle = Color.White.copy(alpha = 0.15f)
    val GlassRadius = 22.dp
}

fun specularBorderBrush(): Brush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.15f),
        Color.Transparent,
        Color.Black.copy(alpha = 0.2f)
    )
)

fun neonSelectionBrush(accent: Color): Brush = Brush.linearGradient(
    colors = listOf(
        accent.copy(alpha = 0.5f),
        accent.copy(alpha = 0.25f),
        accent.copy(alpha = 0.15f)
    )
)

fun obsidianTypography(): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            color = Color.White
        ),
        headlineLarge = base.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            color = Color.White
        ),
        headlineMedium = base.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            color = Color.White
        ),
        titleLarge = base.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            color = Color.White
        ),
        titleMedium = base.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
            color = Color.White
        ),
        titleSmall = base.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
            color = Color.White
        ),
        bodyLarge = base.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
            color = ObsidianPalette.BodyMuted
        ),
        bodyMedium = base.bodyMedium.copy(
            fontWeight = FontWeight.Medium,
            color = ObsidianPalette.BodyMuted
        ),
        bodySmall = base.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            color = ObsidianPalette.BodyMuted.copy(alpha = 0.85f)
        ),
        labelLarge = base.labelLarge.copy(
            fontWeight = FontWeight.Medium,
            color = ObsidianPalette.BodyMuted
        ),
        labelMedium = base.labelMedium.copy(
            fontWeight = FontWeight.Medium,
            color = ObsidianPalette.BodyMuted
        ),
        labelSmall = base.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            color = ObsidianPalette.BodyMuted.copy(alpha = 0.75f)
        )
    )
}

@Composable
fun ObsidianAmbientGlows(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-80).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ObsidianPalette.EmeraldGlow.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 60.dp, y = 120.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ObsidianPalette.GoldGlow.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.BottomStart)
                .offset(x = 40.dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun ObsidianSheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(32.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(ObsidianPalette.Handle)
    )
}

@Composable
fun ObsidianGlassSurface(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    accent: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = ObsidianPalette.GlassRadius,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val fill = if (selected) accent.copy(alpha = 0.1f) else ObsidianPalette.GlassFill
    val borderBrush = if (selected) neonSelectionBrush(accent) else specularBorderBrush()
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = MutableInteractionSource(),
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(clickModifier),
        content = content
    )
}

@Composable
fun ObsidianGlassCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    ObsidianGlassSurface(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        content = content
    )
}
