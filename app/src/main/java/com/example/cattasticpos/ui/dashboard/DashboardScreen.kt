package com.example.cattasticpos.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.cattasticpos.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    LaunchedEffect(uiState.checkoutSuccessEvent) {
        uiState.checkoutSuccessEvent?.let { message ->
            val job = launch { snackbarHostState.showSnackbar(message) }
            kotlinx.coroutines.delay(700)
            job.cancel()
            viewModel.clearCheckoutEvent()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            val job = launch { snackbarHostState.showSnackbar(message) }
            kotlinx.coroutines.delay(700)
            job.cancel()
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Brew ni Cat Logo",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Brew-ni-Cat", fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToInventory) {
                        Icon(imageVector = Icons.Default.Inventory, contentDescription = "Inventory Management")
                    }
                    IconButton(onClick = { viewModel.setShowExpenseDialog(true) }) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "Add Expense")
                    }
                    IconButton(onClick = { viewModel.setShowQueuesDialog(true) }) {
                        Icon(imageVector = Icons.Default.Queue, contentDescription = "View Queues")
                    }
                    IconButton(onClick = { onNavigateToHistory() }) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // TOP SECTION: Menu (Takes up available space)
            Column(modifier = Modifier.weight(1f)) {
                CategorySelector(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    items(uiState.menuItems, key = { it.id }) { item ->
                        val mappedInvId = if (item.categoryId == "cat_drinks") "inv_cups"
                        else when (item.id) {
                            "bite_takoyaki" -> "inv_takoyaki"
                            "bite_fries" -> "inv_fries"
                            "bite_nachos" -> "inv_nachos"
                            else -> null
                        }
                        val invItem = uiState.inventory.find { it.id == mappedInvId }
                        val isLowStock = invItem != null && invItem.currentStock <= invItem.reorderThreshold

                        ItemCard(
                            item = item,
                            isLowStock = isLowStock,
                            onClick = { viewModel.showConfigurationSheet(item) }
                        )
                    }
                }
            }

            // BOTTOM SECTION: Pinned Checkout Sheet
            Surface(
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header & Hold Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Current Order", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                Text(uiState.activeCart.sumOf { it.quantity }.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                        TextButton(
                            onClick = { viewModel.holdCurrentOrder() },
                            enabled = uiState.activeCart.isNotEmpty(),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hold", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .animateContentSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (uiState.activeCart.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                SleepingCatGraphic(
                                    modifier = Modifier.size(64.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No items yet 🐾",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
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

                    // Totals
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Subtotal: ₱${String.format("%.0f", uiState.subtotal)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (uiState.discountDeduction > 0) {
                                Text(
                                    "Disc (${uiState.discountLabel}): -₱${String.format("%.0f", uiState.discountDeduction)}",
                                    fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total: ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₱${String.format("%.0f", uiState.total)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Discounts
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

                    // Checkout Button
                    val btnInteractionSource = remember { MutableInteractionSource() }
                    val btnIsPressed by btnInteractionSource.collectIsPressedAsState()
                    val btnScale by animateFloatAsState(targetValue = if (btnIsPressed) 0.95f else 1f, label = "btnScale")
                    
                    Button(
                        onClick = { viewModel.setShowPaymentDialog(true) },
                        interactionSource = btnInteractionSource,
                        enabled = uiState.activeCart.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().scale(btnScale),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Place Order", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        // Dialogs & Sheets
        if (uiState.selectedConfiguringItem != null) {
            ProductConfigBottomSheet(
                item = uiState.selectedConfiguringItem!!,
                onDismiss = { viewModel.hideConfigurationSheet() },
                onAddToCart = { variant, flavor -> viewModel.addToCart(variant, flavor) }
            )
        }
        if (uiState.showQueuesDialog) {
            QueuesDialog(heldQueues = uiState.heldQueues, onResume = { viewModel.resumeOrder(it) }, onDismiss = { viewModel.setShowQueuesDialog(false) })
        }
        if (uiState.showPaymentDialog) {
            PaymentCheckoutDialog(
                finalTotal = uiState.total,
                onConfirmPayment = { method, ref ->
                    viewModel.setShowPaymentDialog(false)
                    viewModel.confirmCheckout(method, ref)
                },
                onDismiss = { viewModel.setShowPaymentDialog(false) }
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(categories: List<com.example.cattasticpos.domain.model.Category>, selectedCategoryId: String, onCategorySelected: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        items(categories) { category ->
            FilterChip(
                selected = category.id == selectedCategoryId,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCard(item: Item, isLowStock: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "cardScale")

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth().height(120.dp).scale(scale)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (item.categoryId == "cat_drinks") Icons.Default.LocalCafe else Icons.Default.Fastfood, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("₱${String.format("%.0f", item.startingPrice)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                }
            }
            if (isLowStock) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(bottomStart = 8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Low Stock", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
fun PaymentCheckoutDialog(finalTotal: Double, onConfirmPayment: (String, String?) -> Unit, onDismiss: () -> Unit) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var amountTenderedStr by remember { mutableStateOf("") }
    var gcashReference by remember { mutableStateOf("") }
    var receivingAccount by remember { mutableStateOf("Main GCash (0917...)") }
    var simDropdownExpanded by remember { mutableStateOf(false) }
    val simOptions = listOf("Main GCash (0917...)", "Store GCash (0999...)", "Personal GCash")

    val amountTendered = amountTenderedStr.toDoubleOrNull() ?: 0.0
    val changeDue = amountTendered - finalTotal
    val isCash = selectedTabIndex == 0
    val isReady = if (isCash) amountTendered >= finalTotal else receivingAccount.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payment Checkout", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Due:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("₱${String.format("%.0f", finalTotal)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Cash") })
                    Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("GCash") })
                }
                if (isCash) {
                    OutlinedTextField(value = amountTenderedStr, onValueChange = { amountTenderedStr = it }, label = { Text("Amount Tendered (₱)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Change Due:", fontWeight = FontWeight.Medium)
                        Text(if (changeDue >= 0) "₱${String.format("%.0f", changeDue)}" else "---", fontWeight = FontWeight.Medium, color = if (changeDue >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                } else {
                    ExposedDropdownMenuBox(expanded = simDropdownExpanded, onExpandedChange = { simDropdownExpanded = it }) {
                        OutlinedTextField(value = receivingAccount, onValueChange = {}, readOnly = true, label = { Text("Receiving SIM") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = simDropdownExpanded) }, colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(), modifier = Modifier.menuAnchor().fillMaxWidth())
                        ExposedDropdownMenu(expanded = simDropdownExpanded, onDismissRequest = { simDropdownExpanded = false }) {
                            simOptions.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = { receivingAccount = option; simDropdownExpanded = false })
                            }
                        }
                    }
                    OutlinedTextField(value = gcashReference, onValueChange = { gcashReference = it }, label = { Text("GCash Reference No. (Optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = { Button(onClick = { if (isCash) onConfirmPayment("CASH", null) else onConfirmPayment("GCASH", "$receivingAccount - $gcashReference") }, enabled = isReady) { Text("Confirm & Pay") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductConfigBottomSheet(item: Item, onDismiss: () -> Unit, onAddToCart: (Variant, String?) -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var selectedVariant by remember { mutableStateOf(item.variants.firstOrNull() ?: Variant("", "", 0.0)) }
    var selectedFlavor by remember { mutableStateOf(if (item.flavors.isNotEmpty()) item.flavors.first() else null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp).verticalScroll(rememberScrollState())) {
            Text(item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (item.flavors.isNotEmpty()) {
                Text("Select Flavor", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                if (item.id == "drink_coffee") {
                    val grouped = item.flavors.groupBy { if (it.contains(":")) it.substringBefore(":").trim() else "Flavors" }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        grouped.forEach { (group, flavorsInGroup) ->
                            Text(group, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 2.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                flavorsInGroup.forEach { flavor ->
                                    FilterChip(selected = selectedFlavor == flavor, onClick = { selectedFlavor = flavor }, label = { Text(flavor.substringAfter(": ").trim()) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.secondary, selectedLabelColor = MaterialTheme.colorScheme.onSecondary), shape = RoundedCornerShape(8.dp))
                                }
                            }
                        }
                    }
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        item.flavors.forEach { flavor ->
                            FilterChip(selected = selectedFlavor == flavor, onClick = { selectedFlavor = flavor }, label = { Text(flavor) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.secondary, selectedLabelColor = MaterialTheme.colorScheme.onSecondary), shape = RoundedCornerShape(8.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (item.variants.isNotEmpty()) {
                Text("Select Size/Option", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.variants.forEach { variant ->
                        val priceToAdd = variant.getPrice(selectedFlavor)
                        val priceString = if (item.flavors.isNotEmpty() && selectedFlavor == null && variant.priceByFlavor.isNotEmpty()) {
                            " (Select flavor)"
                        } else if (priceToAdd > 0) {
                            " (+₱${String.format("%.0f", priceToAdd)})"
                        } else {
                            ""
                        }
                        FilterChip(selected = selectedVariant.id == variant.id, onClick = { selectedVariant = variant }, label = { Text("${variant.name}$priceString") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.secondary, selectedLabelColor = MaterialTheme.colorScheme.onSecondary), shape = RoundedCornerShape(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (selectedVariant.description != null) {
                    Text("Included in this combo:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(selectedVariant.description!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Price Summary", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("₱${String.format("%.0f", selectedVariant.getPrice(selectedFlavor))}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
                Button(onClick = { onAddToCart(selectedVariant, selectedFlavor) }, shape = RoundedCornerShape(8.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Order", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QueuesDialog(heldQueues: List<HeldQueue>, onResume: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Held Orders Queue 🐾", fontWeight = FontWeight.Bold) },
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
                                        Text("Queue #${queue.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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



