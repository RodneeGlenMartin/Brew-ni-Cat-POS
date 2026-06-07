package com.example.cattasticpos.ui.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Coffee
import com.composables.icons.lucide.Cookie
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Gift
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.LayoutList
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Printer
import com.composables.icons.lucide.ReceiptText
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.ShoppingBag
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.Wallet
import com.example.cattasticpos.ui.adaptive.glassIconGradient

/**
 * Lucide thin-stroke icon layer with optional liquid neon gradient tinting.
 */
object FluentIcons {
    val History: ImageVector = Lucide.History
    val Settings: ImageVector = Lucide.Settings
    val Calendar: ImageVector = Lucide.Calendar
    val Box: ImageVector = Lucide.Package
    val Wallet: ImageVector = Lucide.Wallet
    val List: ImageVector = Lucide.LayoutList
    val Queue: ImageVector = Lucide.Layers
    val ArrowLeft: ImageVector = Lucide.ArrowLeft
    val Print: ImageVector = Lucide.Printer
    val ArrowDownload: ImageVector = Lucide.Download
    val Share: ImageVector = Lucide.Share2
    val Delete: ImageVector = Lucide.Trash2
    val ChevronUp: ImageVector = Lucide.ChevronUp
    val ChevronDown: ImageVector = Lucide.ChevronDown
    val Receipt: ImageVector = Lucide.ReceiptText
    val Trophy: ImageVector = Lucide.Trophy
    val Add: ImageVector = Lucide.Plus
    val Subtract: ImageVector = Lucide.Minus
    val Pause: ImageVector = Lucide.Pause
    val CheckmarkCircle: ImageVector = Lucide.CircleCheck
    val LayoutGrid: ImageVector = Lucide.LayoutGrid
    val ShoppingBag: ImageVector = Lucide.ShoppingBag

    /** Cat-Tastic Drinks — beverage cup. */
    val DrinkCoffee: ImageVector = Lucide.Coffee

    /** Cat-Tastic Bites — pastry / snack silhouette. */
    val FoodBites: ImageVector = Lucide.Cookie

    /** Combos & Packages — grouped package. */
    val ComboPackage: ImageVector = Lucide.Gift

    /** @deprecated Use [FoodBites] or [categoryIcon]. */
    val Food: ImageVector = FoodBites

    fun categoryIcon(categoryId: String): ImageVector = when (categoryId) {
        "cat_drinks" -> DrinkCoffee
        "combos" -> ComboPackage
        else -> FoodBites
    }
}

@Composable
fun FluentIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    size: Dp = 24.dp,
    useGlassGradient: Boolean = true
) {
    val useGradient = useGlassGradient && tint == null
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = if (useGradient) {
            modifier.size(size).glassIconGradient()
        } else {
            modifier.size(size)
        },
        tint = if (useGradient) {
            Color.Unspecified
        } else {
            tint ?: MaterialTheme.colorScheme.primary
        }
    )
}
