package com.example.cattasticpos.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.core.app.ShareCompat
import com.example.cattasticpos.domain.model.ZReadingSummary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import com.example.cattasticpos.ui.icons.FluentIcon
import com.example.cattasticpos.ui.icons.FluentIcons
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import com.example.cattasticpos.domain.model.AppConfig
import com.example.cattasticpos.domain.model.AppThemeAccent
import com.example.cattasticpos.domain.model.Cashier
import com.example.cattasticpos.domain.model.GcashAccount
import com.example.cattasticpos.ui.components.ReceiptPreviewDialog
import com.example.cattasticpos.ui.components.formatReceiptShareText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cattasticpos.domain.model.Order
import com.example.cattasticpos.domain.model.OrderItem
import com.example.cattasticpos.ui.components.SleepingCatGraphic
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.ordersState.collectAsState()
    val grossSales by viewModel.grossSalesState.collectAsState()
    val discounts by viewModel.discountsState.collectAsState()
    val netRevenue by viewModel.netRevenueState.collectAsState()
    val cashSales by viewModel.cashSalesState.collectAsState()
    val gcashSales by viewModel.gcashSalesState.collectAsState()
    val topSellingItem by viewModel.topSellingItemState.collectAsState()
    val expensesList by viewModel.expensesListState.collectAsState()
    val totalExpenses by viewModel.totalExpensesState.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    val appConfig by viewModel.appConfigState.collectAsState()
    val cashierSalesToday by viewModel.cashierSalesTodayState.collectAsState()
    val showDateRangeDialog by viewModel.showDateRangeDialog.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingVoidOrderId by remember { mutableStateOf<String?>(null) }
    var receiptPreviewOrder by remember { mutableStateOf<Order?>(null) }
    
    LaunchedEffect(exportMessage) {
        exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FluentIcon(
                            imageVector = FluentIcons.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Order History Log",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        FluentIcon(
                            imageVector = FluentIcons.ArrowLeft,
                            contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setShowDateRangeDialog(true) }) {
                        FluentIcon(
                            imageVector = FluentIcons.Calendar,
                            contentDescription = "Filter by Date",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            val context = LocalContext.current
            var isZReadingExpanded by remember { mutableStateOf(false) }
            var showConfigDialog by remember { mutableStateOf(false) }
            var startAnimation by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                startAnimation = true
            }

            val todayStart = remember {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            val todayOrders = remember(orders) {
                orders.filter { it.timestamp >= todayStart }
            }
            val totalRevenueToday = remember(todayOrders) {
                todayOrders.sumOf { it.total }
            }
            val totalOrdersProcessed = remember(orders) {
                orders.size
            }

            val startingFloat = appConfig?.startingCashFloat ?: 500.0
            val targetSales = appConfig?.targetSales ?: 5000.0
            val totalCash = cashSales ?: 0.0
            val totalGcash = gcashSales ?: 0.0
            val totalSales = grossSales ?: 0.0
            val expenses = totalExpenses ?: 0.0
            val profits = totalSales - expenses
            val cashDrawer = startingFloat + totalCash - expenses

            val zReadingInteractionSource = remember { MutableInteractionSource() }
            val zReadingPressed by zReadingInteractionSource.collectIsPressedAsState()
            val zReadingScale by animateFloatAsState(if (zReadingPressed) 0.98f else 1f, label = "zReadingScale")

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .scale(zReadingScale)
                    .clickable(
                        interactionSource = zReadingInteractionSource,
                        indication = androidx.compose.foundation.LocalIndication.current
                    ) { isZReadingExpanded = !isZReadingExpanded }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "End of Day Report (Z-Reading)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!isZReadingExpanded) {
                                Text(
                                    text = "Net: ₱${String.format("%.0f", profits)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        
                        OutlinedButton(
                            onClick = {
                                viewModel.printZReading(
                                    ZReadingSummary(
                                        grossSales = totalSales,
                                        discounts = discounts ?: 0.0,
                                        netRevenue = netRevenue ?: 0.0,
                                        cashSales = totalCash,
                                        gcashSales = totalGcash,
                                        totalExpenses = expenses,
                                        startingCashFloat = startingFloat,
                                        cashDrawer = cashDrawer,
                                        profits = profits,
                                        topSellingItem = topSellingItem,
                                        orderCount = orders.size
                                    )
                                )
                            },
                            modifier = Modifier.padding(end = 8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            FluentIcon(
                                imageVector = FluentIcons.Print,
                                contentDescription = "Print Z-Reading",
                                size = 16.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print Z", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.exportData() },
                            modifier = Modifier.padding(end = 8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            FluentIcon(
                                imageVector = FluentIcons.ArrowDownload,
                                contentDescription = "Export CSV",
                                size = 16.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", fontSize = 12.sp)
                        }
                        
                        IconButton(onClick = { showConfigDialog = true }, modifier = Modifier.size(32.dp)) {
                            FluentIcon(
                                imageVector = FluentIcons.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FluentIcon(
                            imageVector = if (isZReadingExpanded) FluentIcons.ChevronUp else FluentIcons.ChevronDown,
                            contentDescription = "Expand/Collapse",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isZReadingExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val animatedTotalSales by animateFloatAsState(
                                targetValue = if (startAnimation) totalSales.toFloat() else 0f,
                                animationSpec = tween(durationMillis = 1000),
                                label = "salesAnim"
                            )
                            val animatedExpenses by animateFloatAsState(
                                targetValue = if (startAnimation) expenses.toFloat() else 0f,
                                animationSpec = tween(durationMillis = 1000),
                                label = "expensesAnim"
                            )
                            val animatedProfits by animateFloatAsState(
                                targetValue = if (startAnimation) profits.toFloat() else 0f,
                                animationSpec = tween(durationMillis = 1000),
                                label = "profitsAnim"
                            )
                            val targetProgress = if (targetSales > 0) (totalSales / targetSales).toFloat().coerceIn(0f, 1f) else 0f
                            val animatedProgress by animateFloatAsState(
                                targetValue = if (startAnimation) targetProgress else 0f,
                                animationSpec = tween(durationMillis = 1000),
                                label = "progressAnim"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Sales (Gross)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    Text("₱${String.format("%.0f", animatedTotalSales)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Expenses", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    Text("-₱${String.format("%.0f", animatedExpenses)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                            
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Goal Progress", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    Text("${(animatedProgress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.2f)
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Profits (Net Cash Flow)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    Text("₱${String.format("%.0f", animatedProfits)}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Cash Drawer Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    Text("₱${String.format("%.0f", cashDrawer)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("(Float: ₱${String.format("%.0f", startingFloat)})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }

                            // Payment Mode Visualization
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Payment Modes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val totalCollected = totalCash + totalGcash
                            val targetCashWeight = if (totalCollected > 0) (totalCash / totalCollected).toFloat() else 0.5f
                            val targetGcashWeight = if (totalCollected > 0) (totalGcash / totalCollected).toFloat() else 0.5f
                            val cashWeight by animateFloatAsState(if (startAnimation) targetCashWeight else 0.5f, animationSpec = tween(1000), label = "cashWeight")
                            val gcashWeight by animateFloatAsState(if (startAnimation) targetGcashWeight else 0.5f, animationSpec = tween(1000), label = "gcashWeight")
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (cashWeight > 0f) {
                                    Box(modifier = Modifier.weight(cashWeight).fillMaxHeight().background(androidx.compose.ui.graphics.Color(0xFF4CAF50), RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = if(gcashWeight == 0f) 8.dp else 0.dp, bottomEnd = if(gcashWeight == 0f) 8.dp else 0.dp)))
                                }
                                if (gcashWeight > 0f) {
                                    Box(modifier = Modifier.weight(gcashWeight).fillMaxHeight().background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp, topStart = if(cashWeight == 0f) 8.dp else 0.dp, bottomStart = if(cashWeight == 0f) 8.dp else 0.dp)))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CASH: ₱${String.format("%.0f", totalCash)} (${(cashWeight * 100).toInt()}%)", fontSize = 10.sp, color = androidx.compose.ui.graphics.Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                Text("GCASH: ₱${String.format("%.0f", totalGcash)} (${(gcashWeight * 100).toInt()}%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            }

                            if (topSellingItem != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FluentIcon(
                                            imageVector = FluentIcons.Trophy,
                                            contentDescription = null,
                                            size = 16.dp,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Best Seller: ${topSellingItem!!.first} - ${topSellingItem!!.second} units",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SleepingCatGraphic(
                            modifier = Modifier.size(64.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No orders found in database.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (expensesList.isNotEmpty()) {
                        item {
                            Text("Expense Timeline", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(expensesList, key = { "exp_${it.id}" }) { expense ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp)).padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(expense.description, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                    Text("By: ${expense.recordedBy}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                                }
                                Text("- ₱${String.format("%.0f", expense.amount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Text("Order Timeline", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                    
                    items(orders, key = { "order_${it.id}" }) { order ->
                        OrderHistoryCard(
                            order = order,
                            onShare = { receiptPreviewOrder = order },
                            onDelete = { pendingVoidOrderId = order.id }
                        )
                    }
                    
                    if (canLoadMore) {
                        item {
                            OutlinedButton(
                                onClick = { viewModel.loadMoreOrders() },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            ) {
                                Text("Load More Orders")
                            }
                        }
                    }
                }
            }

            if (showDateRangeDialog) {
                val dateRangePickerState = rememberDateRangePickerState()
                DatePickerDialog(
                    onDismissRequest = { viewModel.setShowDateRangeDialog(false) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.setDateRange(
                                start = dateRangePickerState.selectedStartDateMillis,
                                end = dateRangePickerState.selectedEndDateMillis?.plus(86399999)
                            )
                            viewModel.setShowDateRangeDialog(false)
                        }) { Text("Apply") }
                    },
                    dismissButton = {
                        Row {
                            TextButton(onClick = {
                                viewModel.setDateRange(null, null)
                                viewModel.setShowDateRangeDialog(false)
                            }) { Text("Clear Filter") }
                            TextButton(onClick = { viewModel.setShowDateRangeDialog(false) }) { Text("Cancel") }
                        }
                    }
                ) {
                    DateRangePicker(
                        state = dateRangePickerState,
                        title = { Text(text = "Select Date Range", modifier = Modifier.padding(16.dp)) },
                        headline = { Text(text = "Filter Orders", modifier = Modifier.padding(horizontal = 16.dp)) },
                        showModeToggle = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (showConfigDialog) {
                EditConfigDialog(
                    initialTarget = appConfig?.targetSales ?: 5000.0,
                    initialFloat = appConfig?.startingCashFloat ?: 500.0,
                    cashiers = appConfig?.cashiers.orEmpty(),
                    activeCashierId = appConfig?.activeCashierId,
                    cashierSalesToday = cashierSalesToday,
                    gcashAccounts = appConfig?.gcashAccounts.orEmpty(),
                    initialThemeAccentId = appConfig?.themeAccentId ?: AppThemeAccent.DEFAULT_ID,
                    onDismiss = { showConfigDialog = false },
                    onSaveWithPin = { target, float, currentPin, newPin ->
                        viewModel.saveConfigWithPinVerification(target, float, currentPin, newPin)
                    },
                    onThemeAccentChange = { viewModel.updateThemeAccent(it) },
                    onSelectActiveCashier = { viewModel.selectActiveCashier(it) },
                    onAddCashier = { viewModel.addCashier(it) },
                    onRemoveCashier = { viewModel.removeCashier(it) },
                    onAddGcashAccount = { viewModel.addGcashAccount(it) },
                    onRemoveGcashAccount = { viewModel.removeGcashAccount(it) }
                )
            }

            pendingVoidOrderId?.let { orderId ->
                VoidOrderDialog(
                    onDismiss = { pendingVoidOrderId = null },
                    onConfirm = { reason ->
                        viewModel.voidOrder(orderId, reason)
                        pendingVoidOrderId = null
                    }
                )
            }

            receiptPreviewOrder?.let { order ->
                ReceiptPreviewDialog(
                    order = order,
                    onDismiss = { receiptPreviewOrder = null },
                    onShare = {
                        shareOrderReceipt(context, order)
                        receiptPreviewOrder = null
                    }
                )
            }
        }
    }
}

@Composable
fun OrderHistoryCard(
    order: Order,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }
    val dateStr = remember(order.timestamp) {
        dateFormatter.format(Date(order.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row: Order ID & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order ID: ${order.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    val badgeColor = if (order.paymentMethod == "GCASH") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.background(badgeColor, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(
                                text = order.paymentMethod,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(24.dp)
                        ) {
                            FluentIcon(
                                imageVector = FluentIcons.Share,
                                contentDescription = "Share Receipt",
                                size = 16.dp
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            FluentIcon(
                                imageVector = FluentIcons.Delete,
                                contentDescription = "Delete Order",
                                size = 16.dp,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateStr,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Items Sold list
            Text(
                text = "Items Sold:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val flavorText = if (item.flavor.isNullOrBlank()) "" else " (${item.flavor.substringAfter(": ").trim()})"
                        Text(
                            text = "${item.quantity} x ${item.itemName} (${item.variantName}$flavorText)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "₱${String.format("%.0f", item.totalPrice)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Bottom row: Discount strategy used & Total payment collected
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Discount Type:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = order.discountLabel.ifBlank { "None" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.discountDeduction > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Payment Collected:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "₱${String.format("%.0f", order.total)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

fun shareOrderReceipt(context: android.content.Context, order: com.example.cattasticpos.domain.model.Order) {
    val text = formatReceiptShareText(order)

    val intent = ShareCompat.IntentBuilder(context)
        .setType("text/plain")
        .setText(text)
        .intent
    context.startActivity(android.content.Intent.createChooser(intent, "Share Receipt"))
}

@Composable
fun VoidOrderDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val reasons = listOf("Wrong order", "Customer cancelled", "Duplicate entry", "Other")
    var selectedReason by remember { mutableStateOf(reasons.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Void Order", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select a reason. Inventory will be restored.", fontSize = 13.sp)
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
                        Text(reason)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedReason) }) {
                Text("Void & Restock")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditConfigDialog(
    initialTarget: Double,
    initialFloat: Double,
    cashiers: List<Cashier>,
    activeCashierId: String?,
    cashierSalesToday: Map<String, Double>,
    gcashAccounts: List<GcashAccount>,
    initialThemeAccentId: String,
    onDismiss: () -> Unit,
    onSaveWithPin: suspend (Double, Double, String, String) -> Boolean,
    onThemeAccentChange: (String) -> Unit,
    onSelectActiveCashier: (String) -> Unit,
    onAddCashier: (String) -> Unit,
    onRemoveCashier: (String) -> Unit,
    onAddGcashAccount: (String) -> Unit,
    onRemoveGcashAccount: (String) -> Unit
) {
    var targetStr by remember { mutableStateOf(if (initialTarget % 1.0 == 0.0) initialTarget.toInt().toString() else initialTarget.toString()) }
    var floatStr by remember { mutableStateOf(if (initialFloat % 1.0 == 0.0) initialFloat.toInt().toString() else initialFloat.toString()) }
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var cashiersExpanded by remember { mutableStateOf(false) }
    var gcashExpanded by remember { mutableStateOf(false) }
    var newCashierName by remember { mutableStateOf("") }
    var newGcashLabel by remember { mutableStateOf("") }
    val selectedAccent = AppThemeAccent.fromId(initialThemeAccentId)
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Business Goals", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target Sales") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = floatStr,
                    onValueChange = { floatStr = it },
                    label = { Text("Starting Cash Float") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Security Setup", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = { value ->
                        currentPin = value.filter { it.isDigit() }.take(4)
                        isError = false
                    },
                    label = { Text("Current PIN (Required to change goals or PIN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text("Incorrect PIN", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { value ->
                        newPin = value.filter { it.isDigit() }.take(4)
                        isError = false
                    },
                    label = { Text("New PIN (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (newPin.isNotEmpty() && newPin.length < 4) {
                    Text("PIN must be 4 digits", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("System Theme Accent", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppThemeAccent.entries.forEach { accent ->
                        val isSelected = accent.id == initialThemeAccentId
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accent.swatch)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { onThemeAccentChange(accent.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.9f))
                                )
                            }
                        }
                    }
                }
                Text(
                    text = selectedAccent.label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                CashierRosterSection(
                    cashiers = cashiers,
                    activeCashierId = activeCashierId,
                    salesByCashier = cashierSalesToday,
                    expanded = cashiersExpanded,
                    onToggle = { cashiersExpanded = !cashiersExpanded },
                    onSelect = onSelectActiveCashier,
                    onRemove = onRemoveCashier,
                    newValue = newCashierName,
                    onNewValueChange = { newCashierName = it },
                    onAdd = {
                        onAddCashier(newCashierName)
                        newCashierName = ""
                    }
                )

                ConfigListSection(
                    title = "GCash SIM Accounts",
                    expanded = gcashExpanded,
                    onToggle = { gcashExpanded = !gcashExpanded },
                    items = gcashAccounts.map { it.label },
                    onRemove = { index -> onRemoveGcashAccount(gcashAccounts[index].id) },
                    newValue = newGcashLabel,
                    onNewValueChange = { newGcashLabel = it },
                    addLabel = "Account Label (e.g. Main GCash 0917...)",
                    onAdd = {
                        onAddGcashAccount(newGcashLabel)
                        newGcashLabel = ""
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        val t = targetStr.toDoubleOrNull() ?: initialTarget
                        val f = floatStr.toDoubleOrNull() ?: initialFloat
                        val goalsChanged = t != initialTarget || f != initialFloat
                        val pinChangeRequested = newPin.length == 4

                        if (!goalsChanged && !pinChangeRequested) {
                            onDismiss()
                            return@launch
                        }

                        if (newPin.isNotEmpty() && newPin.length < 4) {
                            isError = true
                            return@launch
                        }

                        if (currentPin.isBlank()) {
                            isError = true
                            return@launch
                        }

                        if (onSaveWithPin(t, f, currentPin, newPin)) {
                            onDismiss()
                        } else {
                            isError = true
                        }
                    }
                }
            ) {
                Text("Save Goals")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun CashierRosterSection(
    cashiers: List<Cashier>,
    activeCashierId: String?,
    salesByCashier: Map<String, Double>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    newValue: String,
    onNewValueChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Cashiers", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        FluentIcon(
            imageVector = if (expanded) FluentIcons.ChevronUp else FluentIcons.ChevronDown,
            contentDescription = null
        )
    }

    AnimatedVisibility(visible = expanded) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            cashiers.forEach { cashier ->
                val sales = salesByCashier[cashier.id] ?: 0.0
                val isActive = cashier.id == activeCashierId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(cashier.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isActive,
                        onClick = { onSelect(cashier.id) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${cashier.name} (₱${String.format("%.0f", sales)} sales)",
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (isActive) {
                            Text(
                                text = "Active operator",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                    }
                    if (isActive) {
                        FluentIcon(
                            imageVector = FluentIcons.CheckmarkCircle,
                            contentDescription = "Active cashier",
                            size = 18.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    TextButton(onClick = { onRemove(cashier.id) }) {
                        Text("Remove", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newValue,
                    onValueChange = onNewValueChange,
                    label = { Text("Cashier Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onAdd,
                    enabled = newValue.isNotBlank(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
private fun ConfigListSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    items: List<String>,
    onRemove: (Int) -> Unit,
    newValue: String,
    onNewValueChange: (String) -> Unit,
    addLabel: String,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        FluentIcon(
            imageVector = if (expanded) FluentIcons.ChevronUp else FluentIcons.ChevronDown,
            contentDescription = null
        )
    }

    AnimatedVisibility(visible = expanded) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEachIndexed { index, label ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onRemove(index) }) {
                        Text("Remove", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newValue,
                    onValueChange = onNewValueChange,
                    label = { Text(addLabel) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onAdd,
                    enabled = newValue.isNotBlank(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Add")
                }
            }
        }
    }
}



