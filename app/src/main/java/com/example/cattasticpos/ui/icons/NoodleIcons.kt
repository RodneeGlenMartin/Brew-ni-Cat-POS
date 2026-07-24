package com.example.cattasticpos.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom vector icons for Noodle Category, Buldak (Spicy Noodles), and Sedaap (Savory Noodle Bowl).
 */
object NoodleIcons {

    /**
     * Category Icon for "Buldak & Sedaap": A steaming Noodle Bowl with Chopsticks.
     */
    val NoodleCategory: ImageVector by lazy {
        ImageVector.Builder(
            name = "NoodleCategory",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Bowl base
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 11f)
                cubicTo(3f, 16.5f, 7f, 20f, 12f, 20f)
                cubicTo(17f, 20f, 21f, 16.5f, 21f, 11f)
                lineTo(3f, 11f)
                close()
            }
            // Bowl stand
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(9f, 20f)
                lineTo(15f, 20f)
            }
            // Noodle waves / steam lines
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(7f, 8f)
                cubicTo(7.5f, 6.5f, 6.5f, 5f, 7f, 4f)
                moveTo(12f, 8f)
                cubicTo(12.5f, 6.5f, 11.5f, 5f, 12f, 4f)
                moveTo(17f, 8f)
                cubicTo(17.5f, 6.5f, 16.5f, 5f, 17f, 4f)
            }
            // Chopsticks
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(4f, 5f)
                lineTo(19f, 9f)
                moveTo(4f, 7f)
                lineTo(19f, 10.5f)
            }
        }.build()
    }

    /**
     * Product Icon for "Buldak" (Spicy Noodles): Noodle Bowl with Flame / Fire badge.
     */
    val BuldakSpicyNoodle: ImageVector by lazy {
        ImageVector.Builder(
            name = "BuldakSpicyNoodle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Noodle Bowl
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2f, 12f)
                cubicTo(2f, 17f, 6f, 20f, 11f, 20f)
                cubicTo(16f, 20f, 20f, 17f, 20f, 12f)
                lineTo(2f, 12f)
                close()
            }
            // Flame accent
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(11f, 3f)
                cubicTo(11f, 3f, 14f, 5.5f, 14f, 7.5f)
                cubicTo(14f, 9f, 12.8f, 10f, 11f, 10f)
                cubicTo(9.2f, 10f, 8f, 9f, 8f, 7.5f)
                cubicTo(8f, 6.2f, 9.5f, 4.5f, 11f, 3f)
                close()
            }
            // Noodle strand
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(5f, 10f)
                cubicTo(5.5f, 8.5f, 7f, 9f, 8f, 8f)
                moveTo(14f, 8f)
                cubicTo(15f, 9f, 16.5f, 8.5f, 17f, 10f)
            }
        }.build()
    }

    /**
     * Product Icon for "Sedaap" (Indomie / Mi Sedaap Fried Noodles): Noodle Box / Ramen Bowl with Fork/Chopstick.
     */
    val SedaapNoodle: ImageVector by lazy {
        ImageVector.Builder(
            name = "SedaapNoodle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Takeout / Noodle Box contour
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 8f)
                lineTo(6f, 20f)
                lineTo(18f, 20f)
                lineTo(20f, 8f)
                close()
            }
            // Box top flaps
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 8f)
                lineTo(8f, 4f)
                lineTo(12f, 7f)
                lineTo(16f, 4f)
                lineTo(20f, 8f)
            }
            // Noodle loops inside box
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(8f, 12f)
                cubicTo(9f, 14f, 11f, 11f, 12f, 13f)
                cubicTo(13f, 15f, 15f, 12f, 16f, 14f)
            }
        }.build()
    }
}
