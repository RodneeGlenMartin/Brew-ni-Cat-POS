package com.example.cattasticpos.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Remove
import com.example.cattasticpos.ui.adaptive.AdaptiveSnackbarHost
import com.example.cattasticpos.ui.adaptive.CollapsingGlassScaffold
import com.example.cattasticpos.ui.adaptive.LocalCupertinoColors
import com.example.cattasticpos.ui.adaptive.collapsingNestedScroll
import com.example.cattasticpos.ui.adaptive.iOSSpringSpec
import com.example.cattasticpos.ui.adaptive.iOSSpringSize
import com.example.cattasticpos.ui.adaptive.rememberCollapsingHeaderState
import com.example.cattasticpos.ui.adaptive.rememberLiquidGlassHazeState
import com.example.cattasticpos.ui.adaptive.liquidGlassSource
import com.example.cattasticpos.ui.adaptive.liquidGlassChild
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.example.cattasticpos.ui.adaptive.CupertinoSection
import com.example.cattasticpos.ui.adaptive.CupertinoSegmentChip
import com.example.cattasticpos.ui.components.unstyled.PosFilterChip
import com.example.cattasticpos.ui.icons.FluentIcon
import com.example.cattasticpos.ui.icons.FluentIcons
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.model.Variant
import com.example.cattasticpos.domain.strategy.DiscountStrategy
import com.example.cattasticpos.domain.strategy.FreeOrderDiscountStrategy
import com.example.cattasticpos.domain.strategy.NoDiscountStrategy
import com.example.cattasticpos.domain.strategy.PercentageDiscountStrategy
import com.example.cattasticpos.domain.strategy.FivePercentDiscountStrategy
import com.example.cattasticpos.ui.components.SleepingCatGraphic
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.cattasticpos.ui.theme.AlabasterPalette
import com.example.cattasticpos.ui.theme.AdaptiveAmbientGlows
import com.example.cattasticpos.ui.theme.AdaptiveGlassCard
import com.example.cattasticpos.ui.theme.ObsidianGlassCard
import com.example.cattasticpos.ui.theme.ObsidianSheetHandle
import com.example.cattasticpos.ui.theme.adaptiveBodyMuted
import com.example.cattasticpos.ui.theme.adaptiveGlassFill
import com.example.cattasticpos.ui.theme.specularBorderBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToInventory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isCartExpanded by rememberSaveable { mutableStateOf(false) }
    val cartItemCount = uiState.activeCart.sumOf { it.quantity }
    var lastCartItemCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(cartItemCount) {
        when {
            cartItemCount == 0 -> isCartExpanded = false
            lastCartItemCount == 0 && cartItemCount > 0 -> isCartExpanded = true
        }
        lastCartItemCount = cartItemCount
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    val cupertino = LocalCupertinoColors.current
    val hazeState = rememberLiquidGlassHazeState()
    val headerState = rememberCollapsingHeaderState()

    CollapsingGlassScaffold(
        title = "Brew ni Cat",
        hazeState = hazeState,
        headerState = headerState,
        snackbarHost = { AdaptiveSnackbarHost(snackbarHostState) },
        modifier = modifier,
        actions = {
            IconButton(onClick = onNavigateToInventory) {
                Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    FluentIcon(
                        imageVector = FluentIcons.Box,
                        contentDescription = "Inventory Management",
                        tint = cupertino.accent,
                        size = 24.dp
                    )
                }
            }
            IconButton(onClick = { viewModel.setShowExpenseDialog(true) }) {
                Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    FluentIcon(
                        imageVector = FluentIcons.Wallet,
                        contentDescription = "Add Expense",
                        tint = cupertino.accent,
                        size = 24.dp
                    )
                }
            }
            IconButton(onClick = { viewModel.setShowQueuesDialog(true) }) {
                Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    FluentIcon(
                        imageVector = FluentIcons.Queue,
                        contentDescription = "View Queues",
                        tint = cupertino.accent,
                        size = 24.dp
                    )
                }
            }
            IconButton(onClick = { onNavigateToHistory() }) {
                Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    FluentIcon(
                        imageVector = FluentIcons.History,
                        contentDescription = "History",
                        tint = cupertino.accent,
                        size = 24.dp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!uiState.activeTableLabel.isNullOrBlank()) {
                Text(
                    text = "Label: ${uiState.activeTableLabel}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
            // TOP SECTION: Menu (Takes up available space)
            Box(modifier = Modifier.weight(1f)) {
                AdaptiveAmbientGlows(Modifier.fillMaxSize())
                Column(modifier = Modifier.fillMaxSize()) {
                CategorySelector(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .liquidGlassSource(hazeState)
                        .collapsingNestedScroll(headerState)
                ) {
                    items(uiState.menuItems, key = { it.id }) { item ->
                        val itemMappings = uiState.recipeMappings.filter { it.menuItemId == item.id }
                        val isLowStock = if (itemMappings.isEmpty()) {
                            false
                        } else {
                            itemMappings.any { mapping ->
                                val invItem = uiState.inventory.find { it.id == mapping.inventoryItemId }
                                invItem != null && invItem.currentStock <= invItem.reorderThreshold
                            }
                        }

                        ItemCard(
                            item = item,
                            isLowStock = isLowStock,
                            onClick = { viewModel.showConfigurationSheet(item) }
                        )
                    }
                }
                }
            }

            // BOTTOM SECTION: Collapsible Checkout Panel
            val darkTheme = isSystemInDarkTheme()
            val checkoutBorder = if (darkTheme) {
                BorderStroke(1.dp, specularBorderBrush())
            } else {
                BorderStroke(1.dp, AlabasterPalette.RingBorder)
            }
            Surface(
                shadowElevation = 0.dp,
                color = adaptiveGlassFill(darkTheme),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = checkoutBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = iOSSpringSize)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCartExpanded = !isCartExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Current Order ($cartItemCount)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { viewModel.setShowHoldOrderDialog(true) },
                                enabled = uiState.activeCart.isNotEmpty(),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hold", fontSize = 12.sp)
                            }
                            IconButton(onClick = { isCartExpanded = !isCartExpanded }) {
                                Icon(
                                    imageVector = if (isCartExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                    contentDescription = if (isCartExpanded) "Collapse order panel" else "Expand order panel"
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isCartExpanded,
                        enter = expandVertically(animationSpec = iOSSpringSize) + fadeIn(animationSpec = iOSSpringSpec),
                        exit = shrinkVertically(animationSpec = iOSSpringSize) + fadeOut(animationSpec = iOSSpringSpec)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 150.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (uiState.activeCart.isEmpty()) {
                                    Text(
                                        "No items yet",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    uiState.activeCart.forEach { cartItem ->
                                        CartItemRow(
                                            cartItem = cartItem,
                                            onQuantityChange = { id, delta -> viewModel.changeQuantity(id, delta) }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Subtotal: ₱${String.format("%.0f", uiState.subtotal)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (uiState.discountDeduction > 0) {
                                        Text(
                                            "Disc (${uiState.discountLabel}): -₱${String.format("%.0f", uiState.discountDeduction)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Total: ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "₱${String.format("%.0f", uiState.total)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                DiscountButton("None", uiState.selectedDiscountStrategy is NoDiscountStrategy, { viewModel.selectDiscount(NoDiscountStrategy()) }, Modifier.weight(1f))
                                DiscountButton("5%", uiState.selectedDiscountStrategy is FivePercentDiscountStrategy, { viewModel.selectDiscount(FivePercentDiscountStrategy()) }, Modifier.weight(1f))
                                DiscountButton("10%", uiState.selectedDiscountStrategy is PercentageDiscountStrategy && (uiState.selectedDiscountStrategy as PercentageDiscountStrategy).pct == 10.0, { viewModel.selectDiscount(PercentageDiscountStrategy(10.0)) }, Modifier.weight(1f))
                                DiscountButton("20%", uiState.selectedDiscountStrategy is PercentageDiscountStrategy && (uiState.selectedDiscountStrategy as PercentageDiscountStrategy).pct == 20.0, { viewModel.selectDiscount(PercentageDiscountStrategy(20.0)) }, Modifier.weight(1f))
                                DiscountButton("Free", uiState.selectedDiscountStrategy is FreeOrderDiscountStrategy, { viewModel.selectDiscount(FreeOrderDiscountStrategy()) }, Modifier.weight(1.2f))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val btnInteractionSource = remember { MutableInteractionSource() }
                            val btnIsPressed by btnInteractionSource.collectIsPressedAsState()
                            val btnScale by animateFloatAsState(
                                targetValue = if (btnIsPressed) 0.96f else 1f,
                                animationSpec = iOSSpringSpec,
                                label = "btnScale"
                            )

                            Button(
                                onClick = { viewModel.setShowPaymentDialog(true) },
                                interactionSource = btnInteractionSource,
                                enabled = uiState.activeCart.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth().scale(btnScale),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Place Order",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialogs & Sheets
        if (uiState.selectedConfiguringItem != null) {
            key(uiState.selectedConfiguringItem!!.id) {
                ProductConfigBottomSheet(
                    item = uiState.selectedConfiguringItem!!,
                    onDismiss = { viewModel.hideConfigurationSheet() },
                    onAddToCart = { variant, flavor -> viewModel.addToCart(variant, flavor) }
                )
            }
        }
        if (uiState.showQueuesDialog) {
            QueuesDialog(heldQueues = uiState.heldQueues, onResume = { viewModel.resumeOrder(it) }, onDismiss = { viewModel.setShowQueuesDialog(false) })
        }
        if (uiState.showPaymentDialog) {
            PaymentCheckoutDialog(
                finalTotal = uiState.total,
                paymentState = uiState.paymentDialogState,
                gcashAccounts = uiState.gcashAccounts,
                onPaymentStateChange = { viewModel.setPaymentDialogState(it) },
                onConfirmPayment = { method, ref ->
                    viewModel.confirmCheckout(method, ref)
                },
                onDismiss = { viewModel.setShowPaymentDialog(false) }
            )
        }
        if (uiState.showHoldOrderDialog) {
            HoldOrderDialog(
                onHold = { label -> viewModel.holdCurrentOrder(label) },
                onDismiss = { viewModel.setShowHoldOrderDialog(false) }
            )
        }
        if (uiState.showExpenseDialog) {
            AddExpenseDialog(
                onSave = { desc, amount, by -> viewModel.saveExpense(desc, amount, by) },
                onDismiss = { viewModel.setShowExpenseDialog(false) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Shared Components
// ─────────────────────────────────────────────────────────────
@Composable
fun CategorySelector(categories: List<com.example.cattasticpos.domain.model.Category>, selectedCategoryId: String, onCategorySelected: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        items(categories) { category ->
            PosFilterChip(
                selected = category.id == selectedCategoryId,
                onClick = { onCategorySelected(category.id) },
                label = category.name
            )
        }
    }
}

@Composable
fun ItemCard(item: Item, isLowStock: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ObsidianGlassCard(
        modifier = modifier.fillMaxWidth().height(128.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    FluentIcon(
                        imageVector = if (item.categoryId == "cat_drinks") FluentIcons.DrinkCoffee else FluentIcons.Food,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 20.dp
                    )
                }
                Column {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "₱${String.format("%.0f", item.startingPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (isLowStock) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color(0xFFFF6B6B).copy(alpha = 0.2f), RoundedCornerShape(bottomStart = 12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Low Stock", color = Color(0xFFFF8A8A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(cartItem: CartItem, onQuantityChange: (String, Int) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                val variantFlavorText = if (cartItem.flavor.isNullOrBlank()) cartItem.variant.name else "${cartItem.variant.name}/${cartItem.flavor.substringAfter(": ").trim()}"
                Text("${cartItem.quantity}x ${cartItem.item.name} ($variantFlavorText) - ₱${String.format("%.0f", cartItem.totalPrice)}", fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChange(cartItem.id, -1) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp)) }
                IconButton(onClick = { onQuantityChange(cartItem.id, 1) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp)) }
                IconButton(onClick = { onQuantityChange(cartItem.id, -cartItem.quantity) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp)) }
            }
        }
    }
}

@Composable
fun DiscountButton(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(32.dp)
    ) { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCheckoutDialog(
    finalTotal: Double,
    paymentState: PaymentDialogState,
    gcashAccounts: List<com.example.cattasticpos.domain.model.GcashAccount>,
    onPaymentStateChange: (PaymentDialogState) -> Unit,
    onConfirmPayment: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var simDropdownExpanded by remember { mutableStateOf(false) }
    val simOptions = gcashAccounts.map { it.label }

    val amountTendered = paymentState.amountTenderedStr.toDoubleOrNull() ?: 0.0
    val changeDue = amountTendered - finalTotal
    val isCash = paymentState.selectedTabIndex == 0
    val isReady = if (isCash) amountTendered >= finalTotal else paymentState.receivingAccount.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payment Checkout", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Due:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("₱${String.format("%.0f", finalTotal)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
                TabRow(selectedTabIndex = paymentState.selectedTabIndex) {
                    Tab(
                        selected = paymentState.selectedTabIndex == 0,
                        onClick = { onPaymentStateChange(paymentState.copy(selectedTabIndex = 0)) },
                        text = { Text("Cash") }
                    )
                    Tab(
                        selected = paymentState.selectedTabIndex == 1,
                        onClick = { onPaymentStateChange(paymentState.copy(selectedTabIndex = 1)) },
                        text = { Text("GCash") }
                    )
                }
                if (isCash) {
                    OutlinedTextField(
                        value = paymentState.amountTenderedStr,
                        onValueChange = { onPaymentStateChange(paymentState.copy(amountTenderedStr = it)) },
                        label = { Text("Amount Tendered (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Change Due:", fontWeight = FontWeight.Medium)
                        Text(if (changeDue >= 0) "₱${String.format("%.0f", changeDue)}" else "---", fontWeight = FontWeight.Medium, color = if (changeDue >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                } else {
                    ExposedDropdownMenuBox(expanded = simDropdownExpanded, onExpandedChange = { simDropdownExpanded = it }) {
                        OutlinedTextField(
                            value = paymentState.receivingAccount,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Receiving SIM") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = simDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = simDropdownExpanded, onDismissRequest = { simDropdownExpanded = false }) {
                            simOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        onPaymentStateChange(paymentState.copy(receivingAccount = option))
                                        simDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = paymentState.gcashReference,
                        onValueChange = { onPaymentStateChange(paymentState.copy(gcashReference = it)) },
                        label = { Text("GCash Reference No. (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isCash) {
                        onConfirmPayment("CASH", null)
                    } else {
                        val ref = buildString {
                            append("account=${paymentState.receivingAccount}")
                            if (paymentState.gcashReference.isNotBlank()) {
                                append("|ref=${paymentState.gcashReference.trim()}")
                            }
                        }
                        onConfirmPayment("GCASH", ref)
                    }
                },
                enabled = isReady
            ) { Text("Confirm & Pay") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductConfigBottomSheet(item: Item, onDismiss: () -> Unit, onAddToCart: (Variant, String?) -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val contentScrollState = rememberScrollState()
    val hazeState = rememberLiquidGlassHazeState()
    val cupertino = LocalCupertinoColors.current
    var selectedVariant by remember(item.id) {
        mutableStateOf(item.variants.firstOrNull() ?: Variant("", "", 0.0))
    }
    var selectedFlavor by remember(item.id) { mutableStateOf<String?>(null) }
    val hasComboDescriptions = item.variants.any { !it.description.isNullOrBlank() }
    val displayPrice = if (selectedFlavor == null && selectedVariant.basePrice == 0.0) {
        0.0
    } else {
        try {
            selectedVariant.getPrice(selectedFlavor)
        } catch (_: Exception) {
            0.0
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ObsidianSheetHandle(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassChild(state = hazeState)
                        .padding(vertical = 12.dp)
                ) {
                    Text(item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .liquidGlassSource(hazeState)
                    .verticalScroll(contentScrollState)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                if (item.flavors.isNotEmpty()) {
                    CupertinoSection(header = "Select Flavor") {
                    if (item.id == "drink_coffee") {
                        val grouped = item.flavors.groupBy { if (it.contains(":")) it.substringBefore(":").trim() else "Flavors" }
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            grouped.forEach { (group, flavorsInGroup) ->
                                Text(
                                    group,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    flavorsInGroup.forEach { flavor ->
                                        key(flavor) {
                                            CupertinoSegmentChip(
                                                selected = selectedFlavor == flavor,
                                                onClick = { selectedFlavor = flavor },
                                                label = flavor.substringAfter(": ").trim()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        FlowRow(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.flavors.forEach { flavor ->
                                key(flavor) {
                                    CupertinoSegmentChip(
                                        selected = selectedFlavor == flavor,
                                        onClick = { selectedFlavor = flavor },
                                        label = flavor
                                    )
                                }
                            }
                        }
                    }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (item.variants.isNotEmpty()) {
                    CupertinoSection(header = "Select Size/Option") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.variants.forEach { variant ->
                                key(variant.id) {
                                    VariantOptionRow(
                                        variant = variant,
                                        item = item,
                                        selectedFlavor = selectedFlavor,
                                        isSelected = selectedVariant.id == variant.id,
                                        onSelect = { selectedVariant = variant }
                                    )
                                }
                            }
                        }
                    }

                    if (hasComboDescriptions) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Included in this combo:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedVariant.description ?: "Select an option to view combo details.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassChild(state = hazeState)
                    .padding(top = 12.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Price Summary", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(
                        "₱${String.format("%.0f", displayPrice)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Button(
                    onClick = { onAddToCart(selectedVariant, selectedFlavor) },
                    enabled = !(item.flavors.isNotEmpty() && selectedFlavor == null),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Add to Order",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun VariantOptionRow(
    variant: Variant,
    item: Item,
    selectedFlavor: String?,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val priceLabel = formatVariantPriceLabel(variant, item, selectedFlavor)
    val darkTheme = isSystemInDarkTheme()
    val labelColor = if (isSelected) {
        if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    } else {
        adaptiveBodyMuted(darkTheme)
    }

    AdaptiveGlassCard(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        selected = isSelected,
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = variant.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = labelColor
            )
            Text(
                text = priceLabel,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatVariantPriceLabel(variant: Variant, item: Item, selectedFlavor: String?): String {
    if (item.flavors.isNotEmpty() && selectedFlavor == null && variant.priceByFlavor.isNotEmpty()) {
        return "Select flavor"
    }
    val price = try {
        variant.getPrice(selectedFlavor)
    } catch (_: Exception) {
        return "—"
    }
    return "₱${String.format("%.0f", price)}"
}

@Composable
fun QueuesDialog(heldQueues: List<HeldQueue>, onResume: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FluentIcon(
                    imageVector = FluentIcons.Queue,
                    contentDescription = null,
                    size = 20.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Held Orders Queue", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                if (heldQueues.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No held orders in queue.", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        items(heldQueues) { queue ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val labelText = queue.tableLabel?.let { " [$it]" } ?: ""
                                        Text("Queue #${queue.id}$labelText", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        val timeStr = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(queue.timestamp))
                                        Text("Held at: $timeStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        Text("${queue.items.sumOf { it.quantity }} items • ₱${String.format("%.0f", queue.items.sumOf { it.totalPrice })}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(onClick = { onResume(queue.id) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp)) { Text("Resume", fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun HoldOrderDialog(onHold: (String?) -> Unit, onDismiss: () -> Unit) {
    var tableLabel by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hold Order", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = tableLabel,
                onValueChange = { tableLabel = it },
                label = { Text("Table / Label (Optional)") },
                placeholder = { Text("e.g. Table 3, Take-out #5") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onHold(tableLabel) }) { Text("Hold Order") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddExpenseDialog(onSave: (String, Double, String) -> Unit, onDismiss: () -> Unit) {
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var recordedBy by remember { mutableStateOf("") }
    val amount = amountStr.toDoubleOrNull()
    val isReady = description.isNotBlank() && amount != null && amount > 0 && recordedBy.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Expense (from Cash Drawer)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (e.g. Supplies: Ice)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Amount (₱)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = recordedBy, onValueChange = { recordedBy = it }, label = { Text("Recorded By (Name)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { if (isReady) onSave(description, amount!!, recordedBy) }, enabled = isReady) { Text("Save Expense") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}



