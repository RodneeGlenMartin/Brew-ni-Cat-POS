package com.example.cattasticpos.ui.adaptive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.glassIconGradient(): Modifier {
    val darkTheme = isSystemInDarkTheme()
    val primary = MaterialTheme.colorScheme.primary
    val gradientBrush = remember(darkTheme, primary) {
        if (darkTheme) {
            Brush.linearGradient(
                colors = listOf(Color.White, primary),
                start = Offset.Zero,
                end = Offset(48f, 48f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(Color(0xFF333333), Color.Black),
                start = Offset.Zero,
                end = Offset(48f, 48f)
            )
        }
    }
    return drawWithCache {
        onDrawWithContent {
            drawContent()
            drawRect(gradientBrush, blendMode = BlendMode.SrcIn)
        }
    }
}
