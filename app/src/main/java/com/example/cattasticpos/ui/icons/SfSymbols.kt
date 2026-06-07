package com.example.cattasticpos.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private const val STROKE = 1.5f

private fun strokePath(
    name: String,
    block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = STROKE,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = block
    )
}.build()

/** Cookie / snack silhouette for Cat-Tastic Bites. */
val SfCookie: ImageVector = strokePath("SfCookie") {
    moveTo(12f, 4.5f)
    arcTo(7.5f, 7.5f, 0f, true, true, 12f, 19.5f)
    arcTo(7.5f, 7.5f, 0f, true, true, 12f, 4.5f)
    close()
    moveTo(9f, 10f)
    lineTo(9.01f, 10f)
    moveTo(15f, 8.5f)
    lineTo(15.01f, 8.5f)
    moveTo(16f, 13.5f)
    lineTo(16.01f, 13.5f)
    moveTo(10.5f, 15f)
    lineTo(10.51f, 15f)
}

/** Beverage cup silhouette for Cat-Tastic Drinks. */
val SfCup: ImageVector = strokePath("SfCup") {
    moveTo(8f, 4f)
    horizontalLineTo(16f)
    lineTo(15f, 14f)
    curveTo(15f, 16.2f, 13.2f, 18f, 11f, 18f)
    horizontalLineTo(9f)
    curveTo(6.8f, 18f, 5f, 16.2f, 5f, 14f)
    lineTo(4f, 4f)
    close()
    moveTo(16f, 7f)
    horizontalLineTo(18.5f)
    curveTo(19.3f, 7f, 20f, 7.7f, 20f, 8.5f)
    verticalLineTo(9.5f)
    curveTo(20f, 10.3f, 19.3f, 11f, 18.5f, 11f)
    horizontalLineTo(16f)
}

/** Grouped package silhouette for Combos & Packages. */
val SfGiftStack: ImageVector = strokePath("SfGiftStack") {
    moveTo(4f, 9f)
    horizontalLineTo(20f)
    verticalLineTo(19f)
    curveTo(20f, 20.1f, 19.1f, 21f, 18f, 21f)
    horizontalLineTo(6f)
    curveTo(4.9f, 21f, 4f, 20.1f, 4f, 19f)
    verticalLineTo(9f)
    close()
    moveTo(12f, 9f)
    verticalLineTo(21f)
    moveTo(4f, 12f)
    horizontalLineTo(20f)
    moveTo(12f, 9f)
    curveTo(12f, 7.3f, 10.7f, 6f, 9f, 6f)
    curveTo(7.3f, 6f, 6f, 7.3f, 6f, 9f)
    moveTo(12f, 9f)
    curveTo(12f, 7.3f, 13.3f, 6f, 15f, 6f)
    curveTo(16.7f, 6f, 18f, 7.3f, 18f, 9f)
}
