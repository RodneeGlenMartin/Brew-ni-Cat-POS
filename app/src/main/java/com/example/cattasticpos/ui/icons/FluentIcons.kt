package com.example.cattasticpos.ui.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Queue
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SF Symbols–inspired icon layer: thin outlined Material glyphs + custom category vectors.
 */
object FluentIcons {
    val History: ImageVector = Icons.Outlined.History
    val Settings: ImageVector = Icons.Outlined.Settings
    val Calendar: ImageVector = Icons.Outlined.CalendarMonth
    val Box: ImageVector = Icons.Outlined.Inventory2
    val Wallet: ImageVector = Icons.Outlined.AccountBalanceWallet
    val List: ImageVector = Icons.AutoMirrored.Outlined.List
    val Queue: ImageVector = Icons.Outlined.Queue
    val ArrowLeft: ImageVector = Icons.AutoMirrored.Outlined.ArrowBack
    val Print: ImageVector = Icons.Outlined.Print
    val ArrowDownload: ImageVector = Icons.Outlined.Download
    val Share: ImageVector = Icons.Outlined.Share
    val Delete: ImageVector = Icons.Outlined.Delete
    val ChevronUp: ImageVector = Icons.Outlined.ExpandLess
    val ChevronDown: ImageVector = Icons.Outlined.ExpandMore
    val Receipt: ImageVector = Icons.Outlined.ReceiptLong
    val Trophy: ImageVector = Icons.Outlined.EmojiEvents
    val Add: ImageVector = Icons.Outlined.Add
    val Subtract: ImageVector = Icons.Outlined.Remove
    val Pause: ImageVector = Icons.Outlined.Pause
    val CheckmarkCircle: ImageVector = Icons.Outlined.CheckCircle

    /** Cat-Tastic Drinks — beverage cup. */
    val DrinkCoffee: ImageVector = SfCup

    /** Cat-Tastic Bites — snack / cookie plate. */
    val FoodBites: ImageVector = SfCookie

    /** Combos & Packages — grouped gift stack. */
    val ComboPackage: ImageVector = SfGiftStack

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
    tint: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 24.dp
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}
