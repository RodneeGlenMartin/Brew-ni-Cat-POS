package com.example.cattasticpos.ui.adaptive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun GlassWordmark(
    text: String = "Brew ni Cat",
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 34.sp,
    letterSpacing: TextUnit = (-0.5).sp
) {
    val isDark = isSystemInDarkTheme()
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val gradientBrush = remember(isDark, primary, secondary) {
        Brush.linearGradient(
            colors = if (isDark) {
                listOf(Color.White, primary)
            } else {
                listOf(primary, secondary)
            }
        )
    }

    Box(modifier = modifier) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.CenterStart),
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = letterSpacing,
            style = TextStyle(brush = gradientBrush)
        )
    }
}
