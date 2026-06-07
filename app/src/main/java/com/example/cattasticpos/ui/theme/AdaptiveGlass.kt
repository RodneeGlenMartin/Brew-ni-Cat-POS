package com.example.cattasticpos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.cattasticpos.ui.adaptive.BionicHaptic
import com.example.cattasticpos.ui.adaptive.iOSSpring
import com.example.cattasticpos.ui.adaptive.rememberBionicHaptic
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ObsidianPalette {
    val Canvas = Color(0xFF050507)
    val CanvasAlt = Color(0xFF0B0B0C)
    val GlassFill = Color.White.copy(alpha = 0.04f)
    val BodyMuted = Color.White.copy(alpha = 0.7f)
    val EmeraldGlow = Color(0xFF10B981)
    val GoldGlow = Color(0xFFD4AF37)
    val Handle = Color.White.copy(alpha = 0.15f)
    val GlassRadius = 22.dp
}

object AlabasterPalette {
    val Canvas = Color(0xFFF8F9FA)
    val CanvasAlt = Color(0xFFFFFFFF)
    val Heading = Color(0xFF1A1A1E)
    val GlassFill = Color.White.copy(alpha = 0.65f)
    val BodyMuted = Color(0xFF3B3B3B).copy(alpha = 0.85f)
    val MintGlow = Color(0xFFA7F3D0)
    val RoseGoldGlow = Color(0xFFFCA5A5)
    val Handle = Color.Black.copy(alpha = 0.12f)
    val RingBorder = Color.Black.copy(alpha = 0.06f)
    val GlassRadius = 22.dp
}

fun alabasterSpecularBorderBrush(): Brush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.9f),
        Color.Black.copy(alpha = 0.06f),
        Color.White.copy(alpha = 0.4f)
    )
)

@Composable
fun isAdaptiveDarkTheme(): Boolean = isSystemInDarkTheme()

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

@Composable
fun adaptiveGlassFill(darkTheme: Boolean = isAdaptiveDarkTheme()): Color =
    if (darkTheme) ObsidianPalette.GlassFill else AlabasterPalette.GlassFill

@Composable
fun adaptiveBodyMuted(darkTheme: Boolean = isAdaptiveDarkTheme()): Color =
    if (darkTheme) ObsidianPalette.BodyMuted else AlabasterPalette.BodyMuted

@Composable
fun adaptiveGlassRadius(darkTheme: Boolean = isAdaptiveDarkTheme()): Dp =
    if (darkTheme) ObsidianPalette.GlassRadius else AlabasterPalette.GlassRadius

@Composable
fun adaptiveTypography(darkTheme: Boolean = isAdaptiveDarkTheme()): Typography {
    val base = Typography()
    val headingColor = if (darkTheme) Color.White else AlabasterPalette.Heading
    val bodyColor = adaptiveBodyMuted(darkTheme)
    return Typography(
        displayLarge = base.displayLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            color = headingColor
        ),
        headlineLarge = base.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            color = headingColor
        ),
        headlineMedium = base.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            color = headingColor
        ),
        titleLarge = base.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            color = headingColor
        ),
        titleMedium = base.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
            color = headingColor
        ),
        titleSmall = base.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
            color = headingColor
        ),
        bodyLarge = base.bodyLarge.copy(fontWeight = FontWeight.Medium, color = bodyColor),
        bodyMedium = base.bodyMedium.copy(fontWeight = FontWeight.Medium, color = bodyColor),
        bodySmall = base.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            color = bodyColor.copy(alpha = if (darkTheme) 0.85f else 0.9f)
        ),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Medium, color = bodyColor),
        labelMedium = base.labelMedium.copy(fontWeight = FontWeight.Medium, color = bodyColor),
        labelSmall = base.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            color = bodyColor.copy(alpha = if (darkTheme) 0.75f else 0.8f)
        )
    )
}


@Composable
fun AdaptiveAmbientGlows(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isAdaptiveDarkTheme()
) {
    val accentPrimary = MaterialTheme.colorScheme.primary
    val accentSecondary = MaterialTheme.colorScheme.secondary

    Box(modifier = modifier) {
        if (darkTheme) {
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .offset(x = (-80).dp, y = (-40).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentPrimary.copy(alpha = 0.08f),
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
                                accentSecondary.copy(alpha = 0.06f),
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
                                accentPrimary.copy(alpha = 0.07f),
                                Color.Transparent
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = (-60).dp, y = (-30).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentPrimary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 50.dp, y = 100.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentSecondary.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = 30.dp, y = (-50).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentPrimary.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun AdaptiveSheetHandle(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isAdaptiveDarkTheme()
) {
    val handleColor = if (darkTheme) ObsidianPalette.Handle else AlabasterPalette.Handle
    Box(
        modifier = modifier
            .width(32.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(handleColor)
    )
}

@Composable
fun AdaptiveGlassSurface(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    accent: Color = MaterialTheme.colorScheme.primary,
    darkTheme: Boolean = isAdaptiveDarkTheme(),
    cornerRadius: Dp = adaptiveGlassRadius(darkTheme),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val fill = if (selected) accent.copy(alpha = if (darkTheme) 0.1f else 0.12f) else adaptiveGlassFill(darkTheme)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.96f else 1f,
        animationSpec = iOSSpring,
        label = "glassCardScale"
    )
    val performHaptic = rememberBionicHaptic()
    val clickModifier = if (onClick != null) {
        Modifier
            .scale(pressScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    performHaptic(BionicHaptic.Selection)
                    onClick()
                }
            )
    } else {
        Modifier
    }

    val surfaceModifier = if (darkTheme) {
        Modifier.border(width = 1.dp, brush = if (selected) neonSelectionBrush(accent) else specularBorderBrush(), shape = shape)
    } else {
        Modifier.border(
            width = 1.dp,
            brush = if (selected) {
                neonSelectionBrush(accent)
            } else {
                alabasterSpecularBorderBrush()
            },
            shape = shape
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .then(surfaceModifier)
            .then(clickModifier),
        content = content
    )
}

@Composable
fun AdaptiveGlassCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    AdaptiveGlassSurface(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        content = content
    )
}

@Composable
fun ObsidianAmbientGlows(modifier: Modifier = Modifier) = AdaptiveAmbientGlows(modifier)

@Composable
fun ObsidianSheetHandle(modifier: Modifier = Modifier) = AdaptiveSheetHandle(modifier)

@Composable
fun ObsidianGlassSurface(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    accent: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = ObsidianPalette.GlassRadius,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) = AdaptiveGlassSurface(modifier, selected, accent, cornerRadius = cornerRadius, onClick = onClick, content = content)

@Composable
fun ObsidianGlassCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) = AdaptiveGlassCard(modifier, selected, onClick, content)
