package com.example.cattasticpos.ui.adaptive

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlassWordmark(
    text: String = "Cattastic",
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 34.sp,
    letterSpacing: TextUnit = (-0.5).sp
) {
    val primary = MaterialTheme.colorScheme.primary
    val gradientBrush = remember(primary) {
        Brush.horizontalGradient(
            colors = listOf(Color.White, primary)
        )
    }

    Text(
        text = text,
        modifier = modifier.shadow(
            elevation = 12.dp,
            spotColor = primary.copy(alpha = 0.45f),
            ambientColor = Color.Black.copy(alpha = 0.25f)
        ),
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = letterSpacing,
        style = TextStyle(brush = gradientBrush)
    )
}
