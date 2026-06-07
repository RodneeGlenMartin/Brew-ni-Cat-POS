package com.example.cattasticpos.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun SleepingCatGraphic(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    val sleepingCatVector = ImageVector.Builder(
        name = "SleepingCat",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Face outline
        path(fill = SolidColor(color)) {
            moveTo(12f, 22f)
            curveTo(5.4f, 22f, 0f, 16.6f, 0f, 10f)
            lineTo(3f, 2f)
            lineTo(8.5f, 5.5f)
            curveTo(10.7f, 4.8f, 13.3f, 4.8f, 15.5f, 5.5f)
            lineTo(21f, 2f)
            lineTo(24f, 10f)
            curveTo(24f, 16.6f, 18.6f, 22f, 12f, 22f)
            close()
        }
        // Closed eyes (sleeping)
        val detailColor = if (color.luminance() > 0.5f) Color.Black else Color.White
        path(stroke = SolidColor(detailColor), strokeLineWidth = 1.5f) {
            moveTo(6f, 12f)
            curveTo(7f, 14f, 9f, 14f, 10f, 12f)
            moveTo(14f, 12f)
            curveTo(15f, 14f, 17f, 14f, 18f, 12f)
        }
        // Nose
        path(fill = SolidColor(detailColor)) {
            moveTo(11.2f, 16f)
            lineTo(12.8f, 16f)
            lineTo(12f, 17f)
            close()
        }
    }.build()

    Icon(
        imageVector = sleepingCatVector,
        contentDescription = "Sleeping Cat Graphic",
        modifier = modifier,
        tint = Color.Unspecified // Tint is baked into paths
    )
}
