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
import com.composables.icons.lucide.CircleDot
import com.composables.icons.lucide.Coffee
import com.composables.icons.lucide.Cookie
import com.composables.icons.lucide.CupSoda
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.LayoutList
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Percent
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Printer
import com.composables.icons.lucide.ReceiptText
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Sandwich
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.ShoppingBag
import com.composables.icons.lucide.Tag
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.User
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.UsersRound
import com.composables.icons.lucide.Utensils
import com.composables.icons.lucide.Wallet
import com.composables.icons.lucide.X
import com.example.cattasticpos.ui.adaptive.glassIconGradient

/**
 * Lucide icon registry. Default rendering uses glass gradient via [FluentIcon];
 * pass explicit [tint] + useGlassGradient=false for toolbar chrome only.
 */
object FluentIcons {
    val History: ImageVector = Lucide.History
    val Settings: ImageVector = Lucide.Settings
    val Search: ImageVector = Lucide.Search
    val Calendar: ImageVector = Lucide.Calendar
    val Box: ImageVector = Lucide.Package
    val Wallet: ImageVector = Lucide.Wallet
    val List: ImageVector = Lucide.LayoutList
    val Queue: ImageVector = Lucide.Layers
    val ArrowLeft: ImageVector = Lucide.ArrowLeft
    val Print: ImageVector = Lucide.Printer
    val ArrowDownload: ImageVector = Lucide.Download
    val Share: ImageVector = Lucide.Share2
    val Edit: ImageVector = Lucide.Pencil
    val Delete: ImageVector = Lucide.Trash2
    val ChevronUp: ImageVector = Lucide.ChevronUp
    val ChevronDown: ImageVector = Lucide.ChevronDown
    val Receipt: ImageVector = Lucide.ReceiptText
    val Trophy: ImageVector = Lucide.Trophy
    val Add: ImageVector = Lucide.Plus
    val Subtract: ImageVector = Lucide.Minus
    val Pause: ImageVector = Lucide.Pause
    val CheckmarkCircle: ImageVector = Lucide.CircleCheck
    val Close: ImageVector = Lucide.X
    val Tag: ImageVector = Lucide.Tag
    val Percent: ImageVector = Lucide.Percent
    val ShoppingBag: ImageVector = Lucide.ShoppingBag
    val Utensils: ImageVector = Lucide.Utensils

    val DrinkCoffee: ImageVector = Lucide.Coffee
    val FoodBites: ImageVector = Lucide.Cookie
    val ComboPackage: ImageVector = Lucide.Utensils

    fun categoryIcon(categoryId: String): ImageVector = when (categoryId) {
        "cat_drinks" -> DrinkCoffee
        "combos" -> ComboPackage
        else -> FoodBites
    }

    fun menuItemIcon(itemId: String): ImageVector = when (itemId) {
        "drink_cat_feine" -> DrinkCoffee
        "drink_oreo" -> Lucide.Cookie
        "drink_matcha" -> Lucide.Leaf
        "drink_soda" -> Lucide.CupSoda
        "drink_coffee" -> DrinkCoffee
        "bite_takoyaki" -> Lucide.CircleDot
        "bite_fries" -> Lucide.Flame
        "bite_nachos" -> Lucide.Sandwich
        "combo_single_paw", "combo_meals" -> Lucide.User
        "combo_couple_cats" -> Lucide.Users
        "combo_association" -> Lucide.UsersRound
        else -> categoryIcon(
            when {
                itemId.startsWith("drink_") -> "cat_drinks"
                itemId.startsWith("combo_") -> "combos"
                else -> "cat_bites"
            }
        )
    }
}

@Composable
fun FluentIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    size: Dp = PosIconSize.Default,
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
