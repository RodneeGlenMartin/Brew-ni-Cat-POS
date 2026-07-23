package com.example.cattasticpos.ui.adaptive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun rememberLiquidGlassHazeState(): HazeState = remember { HazeState() }

@Composable
private fun resolveHazeBackgroundColor(darkTheme: Boolean): Color {
    val themeSurface = MaterialTheme.colorScheme.surface
    if (themeSurface.isSpecified) return themeSurface
    return if (darkTheme) Color(0xFF1C1B1F) else Color(0xFFFFFBFE)
}

@Composable
fun liquidGlassHazeStyle(
    darkTheme: Boolean = isSystemInDarkTheme(),
    intensity: Float = 1f
): HazeStyle {
    val effective = intensity.coerceIn(0f, 1f)
    val backgroundColor = resolveHazeBackgroundColor(darkTheme)
    // Blur radius dominates real-time glass cost on the tablet GPU; 16dp keeps the frosted look
    // but is meaningfully cheaper to recompute while content scrolls behind the header/cart.
    return if (darkTheme) {
        HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(Color.Black.copy(alpha = 0.22f * effective)),
            blurRadius = (16f * effective).dp,
            noiseFactor = 0.08f * effective
        )
    } else {
        HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(Color.White.copy(alpha = 0.5f * effective)),
            blurRadius = (16f * effective).dp,
            noiseFactor = 0.03f * effective
        )
    }
}

fun Modifier.liquidGlassSource(state: HazeState): Modifier = haze(state = state)

@Composable
fun Modifier.liquidGlassChild(
    state: HazeState,
    darkTheme: Boolean = isSystemInDarkTheme(),
    intensity: Float = 1f
): Modifier {
    val effective = intensity.coerceIn(0f, 1f)
    if (effective <= 0f) return this
    val backgroundColor = resolveHazeBackgroundColor(darkTheme)
    return hazeChild(state = state, style = liquidGlassHazeStyle(darkTheme, effective)) {
        this.backgroundColor = backgroundColor
    }
}
