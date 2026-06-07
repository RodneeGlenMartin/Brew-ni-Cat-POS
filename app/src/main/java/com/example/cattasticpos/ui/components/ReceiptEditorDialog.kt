package com.example.cattasticpos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.CartKey
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.model.Order
import com.example.cattasticpos.domain.model.Variant
import com.example.cattasticpos.domain.strategy.DiscountStrategy
import com.example.cattasticpos.domain.strategy.FivePercentDiscountStrategy
import com.example.cattasticpos.domain.strategy.FreeOrderDiscountStrategy
import com.example.cattasticpos.domain.strategy.NoDiscountStrategy
import com.example.cattasticpos.domain.strategy.PercentageDiscountStrategy
import com.example.cattasticpos.domain.usecase.CalculateCartUseCase
import com.example.cattasticpos.domain.usecase.OrderCartMapper
import com.example.cattasticpos.ui.components.unstyled.PosButtonIconLabel
import com.example.cattasticpos.ui.components.unstyled.PosPrimaryButton
import com.example.cattasticpos.ui.dashboard.DiscountButton
import com.example.cattasticpos.ui.dashboard.ProductConfigBottomSheet
import com.example.cattasticpos.ui.icons.FluentIcon
import com.example.cattasticpos.ui.icons.FluentIcons
import com.example.cattasticpos.ui.theme.AdaptiveGlassDialog

@Composable
fun ReceiptEditorDialog(
    order: Order,
    menuItems: List<Item>,
    onDismiss: () -> Unit,
    onSave: (List<CartItem>, DiscountStrategy) -> Unit,
    onShare: (Order) -> Unit
) {
    val calculateCart = remember { CalculateCartUseCase() }
    var editItems by remember(order.id, menuItems) {
        mutableStateOf(OrderCartMapper.orderToCartItems(order, menuItems))
    }
    var discountStrategy by remember(order.id) {
        mutableStateOf(OrderCartMapper.discountStrategyFromLabel(order.discountLabel))
    }
    var showMenuPicker by remember { mutableStateOf(false) }
    var configuringItem by remember { mutableStateOf<Item?>(null) }

    val calculation = remember(editItems, discountStrategy) {
        calculateCart(editItems, discountStrategy)
    }
    val previewOrder = remember(order, editItems, calculation) {
        OrderCartMapper.previewOrder(order, editItems, calculation)
    }

    fun updateQuantity(cartItemId: String, delta: Int) {
        editItems = editItems.mapNotNull { item ->
            if (item.id != cartItemId) item
            else {
                val newQty = item.quantity + delta
                if (newQty <= 0) null else item.copy(quantity = newQty)
            }
        }
    }

    fun addConfiguredItem(variant: Variant, flavor: String?) {
        val item = configuringItem ?: return
        val cartKey = CartKey.from(item, variant, flavor)
        val existingIndex = editItems.indexOfFirst { it.key == cartKey }
        editItems = if (existingIndex >= 0) {
            editItems.mapIndexed { index, cartItem ->
                if (index == existingIndex) cartItem.copy(quantity = cartItem.quantity + 1) else cartItem
            }
        } else {
            editItems + CartItem(
                key = cartKey,
                item = item,
                variant = variant,
                flavor = flavor,
                quantity = 1
            )
        }
        configuringItem = null
    }

    AdaptiveGlassDialog(
        onDismissRequest = onDismiss,
        surfaceAlpha = 0.93f,
        fixedFooter = true,
        contentMaxHeight = 360.dp,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Receipt #${order.receiptNumber}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "₱${String.format("%.0f", calculation.total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            PosPrimaryButton(
                onClick = { onSave(editItems, discountStrategy) },
                enabled = editItems.isNotEmpty()
            ) {
                Text(
                    text = "Save Receipt",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThermalReceiptCard(order = previewOrder)

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Line Items", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedButton(
                        onClick = { showMenuPicker = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        PosButtonIconLabel(
                            icon = {
                                FluentIcon(
                                    imageVector = FluentIcons.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 16.dp
                                )
                            },
                            label = "Add Item",
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (editItems.isEmpty()) {
                    Text(
                        "No items on this receipt. Tap Add Item to add products.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        editItems.forEach { cartItem ->
                            ReceiptEditLineRow(
                                cartItem = cartItem,
                                onDecrease = { updateQuantity(cartItem.id, -1) },
                                onIncrease = { updateQuantity(cartItem.id, 1) },
                                onRemove = { updateQuantity(cartItem.id, -cartItem.quantity) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DiscountButton(
                        "None",
                        discountStrategy is NoDiscountStrategy,
                        { discountStrategy = NoDiscountStrategy() },
                        Modifier.weight(1f)
                    )
                    DiscountButton(
                        "5%",
                        discountStrategy is FivePercentDiscountStrategy,
                        { discountStrategy = FivePercentDiscountStrategy() },
                        Modifier.weight(1f)
                    )
                    DiscountButton(
                        "10%",
                        discountStrategy is PercentageDiscountStrategy && (discountStrategy as PercentageDiscountStrategy).pct == 10.0,
                        { discountStrategy = PercentageDiscountStrategy(10.0) },
                        Modifier.weight(1f)
                    )
                    DiscountButton(
                        "20%",
                        discountStrategy is PercentageDiscountStrategy && (discountStrategy as PercentageDiscountStrategy).pct == 20.0,
                        { discountStrategy = PercentageDiscountStrategy(20.0) },
                        Modifier.weight(1f)
                    )
                    DiscountButton(
                        "Free",
                        discountStrategy is FreeOrderDiscountStrategy,
                        { discountStrategy = FreeOrderDiscountStrategy() },
                        Modifier.weight(1.2f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onShare(previewOrder) }) {
                        FluentIcon(
                            imageVector = FluentIcons.Share,
                            contentDescription = null,
                            size = 16.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            }
        }
    )

    if (showMenuPicker) {
        ReceiptMenuPickerDialog(
            menuItems = menuItems,
            onDismiss = { showMenuPicker = false },
            onSelectItem = { item ->
                showMenuPicker = false
                configuringItem = item
            }
        )
    }

    configuringItem?.let { item ->
        ProductConfigBottomSheet(
            item = item,
            onDismiss = { configuringItem = null },
            onAddToCart = { variant, flavor -> addConfiguredItem(variant, flavor) }
        )
    }
}

@Composable
private fun ReceiptEditLineRow(
    cartItem: CartItem,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val flavorText = if (cartItem.flavor.isNullOrBlank()) {
                    cartItem.variant.name
                } else {
                    "${cartItem.variant.name}/${cartItem.flavor.substringAfter(": ").trim()}"
                }
                Text(
                    "${cartItem.quantity}x ${cartItem.item.name} ($flavorText)",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "₱${String.format("%.0f", cartItem.totalPrice)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(24.dp)) {
                    FluentIcon(imageVector = FluentIcons.Subtract, contentDescription = null, size = 14.dp)
                }
                IconButton(onClick = onIncrease, modifier = Modifier.size(24.dp)) {
                    FluentIcon(
                        imageVector = FluentIcons.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 14.dp
                    )
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    FluentIcon(
                        imageVector = FluentIcons.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        size = 14.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptMenuPickerDialog(
    menuItems: List<Item>,
    onDismiss: () -> Unit,
    onSelectItem: (Item) -> Unit
) {
    AdaptiveGlassDialog(
        onDismissRequest = onDismiss,
        surfaceAlpha = 0.93f,
        title = { Text("Add Item to Receipt", fontWeight = FontWeight.Bold) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(menuItems, key = { it.id }) { item ->
                    OutlinedButton(
                        onClick = { onSelectItem(item) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                item.name,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )
                            Text(
                                "₱${String.format("%.0f", item.startingPrice)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    )
}
