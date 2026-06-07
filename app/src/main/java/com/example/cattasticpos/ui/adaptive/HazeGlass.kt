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
fun liquidGlassHazeStyle(): HazeStyle {
    return HazeStyle(
        blurRadius = 24.dp,
        tint = HazeTint(Color.Black.copy(alpha = 0.2f)),
        noiseFactor = 0.12f
    )
}

fun Modifier.liquidGlassSource(state: HazeState): Modifier = haze(state = state)

@Composable
fun Modifier.liquidGlassChild(
    state: HazeState
): Modifier = hazeChild(state = state, style = liquidGlassHazeStyle()) {
    progressive = HazeProgressive.verticalGradient(
        startIntensity = 1f,
        endIntensity = 0.35f
    )
}
