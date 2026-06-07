package com.example.cattasticpos.ui.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fluent UI System Icons integration layer (24dp regular sizing spec).
 * Uses Material vector glyphs with Fluent-aligned proportions and accent-aware tinting.
 */
object FluentIcons {
    val History: ImageVector = Icons.Filled.History
    val Settings: ImageVector = Icons.Filled.Settings
    val Calendar: ImageVector = Icons.Filled.CalendarMonth
    val Box: ImageVector = Icons.Filled.Inventory2
    val Wallet: ImageVector = Icons.Filled.Wallet
    val List: ImageVector = Icons.AutoMirrored.Filled.List
    val Queue: ImageVector = Icons.Filled.Queue
    val ArrowLeft: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val Print: ImageVector = Icons.Filled.Print
    val ArrowDownload: ImageVector = Icons.Filled.Download
    val Share: ImageVector = Icons.Filled.Share
    val Delete: ImageVector = Icons.Filled.Delete
    val ChevronUp: ImageVector = Icons.Filled.ExpandLess
    val ChevronDown: ImageVector = Icons.Filled.ExpandMore
    val Receipt: ImageVector = Icons.Filled.ReceiptLong
    val Trophy: ImageVector = Icons.Filled.EmojiEvents
    val Add: ImageVector = Icons.Filled.Add
    val Subtract: ImageVector = Icons.Filled.Remove
    val Pause: ImageVector = Icons.Filled.Pause
    val CheckmarkCircle: ImageVector = Icons.Filled.CheckCircle
    val DrinkCoffee: ImageVector = Icons.Filled.LocalCafe
    val Food: ImageVector = Icons.Filled.Fastfood
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
