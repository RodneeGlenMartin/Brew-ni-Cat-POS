package com.example.cattasticpos.ui.adaptive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun rememberLiquidGlassHazeState(): HazeState = remember { HazeState() }

@Composable
fun liquidGlassHazeStyle(
    darkTheme: Boolean = isSystemInDarkTheme(),
    intensity: Float = 1f
): HazeStyle {
    val effective = intensity.coerceIn(0f, 1f)
    val backgroundColor = MaterialTheme.colorScheme.surface
    return if (darkTheme) {
        HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(Color.Black.copy(alpha = 0.2f * effective)),
            blurRadius = (24f * effective).dp,
            noiseFactor = 0.12f * effective
        )
    } else {
        HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(Color.White.copy(alpha = 0.45f * effective)),
            blurRadius = (24f * effective).dp,
            noiseFactor = 0.05f * effective
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
    val endIntensityFactor = if (darkTheme) 0.35f else 0.5f
    val backgroundColor = MaterialTheme.colorScheme.surface
    return hazeChild(state = state, style = liquidGlassHazeStyle(darkTheme, effective)) {
        this.backgroundColor = backgroundColor
        progressive = HazeProgressive.verticalGradient(
            startIntensity = effective,
            endIntensity = effective * endIntensityFactor
        )
    }
}
