package com.example.cattasticpos.ui.adaptive

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
fun liquidGlassHazeStyle(accent: Color = LocalCupertinoColors.current.accent): HazeStyle {
    return HazeStyle(
        blurRadius = 22.dp,
        tint = HazeTint(accent.copy(alpha = 0.2f)),
        noiseFactor = 0.1f
    )
}

fun Modifier.liquidGlassSource(state: HazeState): Modifier = haze(state = state)

@Composable
fun Modifier.liquidGlassChild(
    state: HazeState,
    accent: Color = LocalCupertinoColors.current.accent
): Modifier = hazeChild(state = state, style = liquidGlassHazeStyle(accent)) {
    progressive = HazeProgressive.verticalGradient(
        startIntensity = 1f,
        endIntensity = 0.4f
    )
}
