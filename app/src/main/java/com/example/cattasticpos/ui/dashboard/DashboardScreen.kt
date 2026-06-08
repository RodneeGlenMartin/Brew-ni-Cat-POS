package com.example.cattasticpos.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.cattasticpos.ui.adaptive.AdaptiveSnackbarHost
import com.example.cattasticpos.ui.adaptive.BrewNiCatBrandIcon
import com.example.cattasticpos.ui.adaptive.CollapsingGlassScaffold
import com.example.cattasticpos.ui.adaptive.CollapsingHeaderState
import com.example.cattasticpos.ui.adaptive.LocalCupertinoColors
import com.example.cattasticpos.ui.adaptive.collapsingNestedScroll
import com.example.cattasticpos.ui.adaptive.iOSSpringSpec
import com.example.cattasticpos.ui.adaptive.iOSSpringDp
import com.example.cattasticpos.ui.adaptive.iOSSpringSize
import com.example.cattasticpos.ui.adaptive.liquidSwipeTransition
import com.example.cattasticpos.ui.adaptive.rememberCollapsingHeaderState
import com.example.cattasticpos.ui.adaptive.rememberLiquidGlassHazeState
import com.example.cattasticpos.ui.adaptive.liquidGlassSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.example.cattasticpos.ui.adaptive.CupertinoSection
import com.example.cattasticpos.ui.adaptive.BionicHaptic
import com.example.cattasticpos.ui.adaptive.CupertinoSegmentChip
import com.example.cattasticpos.ui.adaptive.rememberBionicHaptic
import com.example.cattasticpos.ui.components.unstyled.PosFilterChip
import com.example.cattasticpos.ui.icons.FluentIcon
import com.example.cattasticpos.ui.icons.FluentIcons
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.Item
import dev.chrisbanes.haze.HazeState
import com.example.cattasticpos.domain.model.Variant
import com.example.cattasticpos.domain.strategy.DiscountStrategy
import com.example.cattasticpos.domain.strategy.FreeOrderDiscountStrategy
import com.example.cattasticpos.domain.strategy.NoDiscountStrategy
import com.example.cattasticpos.domain.strategy.PercentageDiscountStrategy
import com.example.cattasticpos.domain.strategy.FivePercentDiscountStrategy
import com.example.cattasticpos.ui.components.GlassSearchBar
import androidx.compose.foundation.layout.widthIn
import com.example.cattasticpos.ui.components.SleepingCatGraphic
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.cattasticpos.ui.theme.AlabasterPalette
import com.example.cattasticpos.ui.components.unstyled.PosButtonIconLabel
import com.example.cattasticpos.ui.components.unstyled.PosPrimaryButton
import com.example.cattasticpos.ui.theme.AdaptiveAmbientGlows
import com.example.cattasticpos.ui.theme.AdaptiveGlassDialog
import com.example.cattasticpos.ui.theme.AdaptiveGlassCard
import com.example.cattasticpos.ui.theme.adaptiveGlassBrush
import com.example.cattasticpos.ui.theme.adaptiveBodyMuted
import com.example.cattasticpos.ui.theme.adaptiveGlassContentColor
import com.example.cattasticpos.ui.theme.ObsidianGlassCard
import com.example.cattasticpos.ui.theme.ObsidianGlassSurface
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
    var isCartExpanded by remember { mutableStateOf(false) }
    val cartItemCount = uiState.activeCart.sumOf { it.quantity }

    LaunchedEffect(cartItemCount) {
        if (cartItemCount == 0) {
            isCartExpanded = false
        }
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
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    val isCatalogSearchActive = isSearchExpanded || searchQuery.isNotEmpty()

    LaunchedEffect(isCatalogSearchActive) {
        if (isCatalogSearchActive) {
            headerState.resetCollapse()
        }
    }

    CollapsingGlassScaffold(
        title = "Brew ni Cat",
        hazeState = hazeState,
        headerState = headerState,
        showBrandWordmark = true,
        snackbarHost = { AdaptiveSnackbarHost(snackbarHostState) },
        modifier = modifier,
        navigationIcon = {
            BrewNiCatBrandIcon(
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        actions = {
            DashboardHeaderIconButton(
                onClick = {
                    if (isSearchExpanded) {
                        isSearchExpanded = false
                        searchQuery = ""
                    } else {
                        headerState.resetCollapse()
                        isSearchExpanded = true
                    }
                },
                icon = FluentIcons.Search,
                contentDescription = "Search menu",
                tint = cupertino.accent
            )
            DashboardHeaderIconButton(
                onClick = onNavigateToInventory,
                icon = FluentIcons.Box,
                contentDescription = "Inventory Management",
                tint = cupertino.accent
            )
            DashboardHeaderIconButton(
                onClick = { viewModel.setShowExpenseDialog(true) },
                icon = FluentIcons.Wallet,
                contentDescription = "Add Expense",
                tint = cupertino.accent
            )
            DashboardHeaderIconButton(
                onClick = { viewModel.setShowQueuesDialog(true) },
                icon = FluentIcons.Queue,
                contentDescription = "View Queues",
                tint = cupertino.accent
            )
            DashboardHeaderIconButton(
                onClick = onNavigateToHistory,
                icon = FluentIcons.History,
                contentDescription = "History",
                tint = cupertino.accent
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!uiState.activeTableLabel.isNullOrBlank() && cartItemCount > 0) {
                Text(
                    text = "Label: ${uiState.activeTableLabel}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
            val isTablet = LocalConfiguration.current.screenWidthDp >= 600
            val darkTheme = isSystemInDarkTheme()
            val checkoutBorder = if (darkTheme) {
                BorderStroke(1.dp, specularBorderBrush())
            } else {
                BorderStroke(1.dp, AlabasterPalette.RingBorder)
            }

            if (isTablet) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    StorefrontCatalogPane(
                        modifier = Modifier.weight(1f),
                        uiState = uiState,
                        hazeState = hazeState,
                        headerState = headerState,
                        searchQuery = searchQuery,
                        isSearchExpanded = isSearchExpanded,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchExpandedChange = { isSearchExpanded = it },
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onItemClick = { viewModel.showConfigurationSheet(it) },
                        compactGlows = false
                    )
                    DashboardCheckoutPanel(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)),
                        uiState = uiState,
                        cartItemCount = cartItemCount,
                        isCartExpanded = true,
                        onCartExpandedChange = { isCartExpanded = it },
                        checkoutBorder = checkoutBorder,
                        darkTheme = darkTheme,
                        onHoldOrder = { viewModel.setShowHoldOrderDialog(true) },
                        onPlaceOrder = { viewModel.setShowPaymentDialog(true) },
                        onQuantityChange = { id, delta -> viewModel.changeQuantity(id, delta) },
                        onSelectDiscount = { viewModel.selectDiscount(it) },
                        forceExpanded = true,
                        useBottomSheetStyle = false
                    )
                }
            } else {
                val cartBottomPadding by animateDpAsState(
                    targetValue = if (isCartExpanded) 380.dp else 120.dp,
                    animationSpec = iOSSpringDp,
                    label = "cartPadding"
                )
                Column(modifier = Modifier.weight(1f)) {
                    StorefrontCatalogPane(
                        modifier = Modifier.weight(1f),
                        uiState = uiState,
                        hazeState = hazeState,
                        headerState = headerState,
                        searchQuery = searchQuery,
                        isSearchExpanded = isSearchExpanded,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchExpandedChange = { isSearchExpanded = it },
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onItemClick = { viewModel.showConfigurationSheet(it) },
                        compactGlows = true,
                        bottomContentPadding = cartBottomPadding
                    )
                    DashboardCheckoutPanel(
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState,
                        cartItemCount = cartItemCount,
                        isCartExpanded = isCartExpanded,
                        onCartExpandedChange = { isCartExpanded = it },
                        checkoutBorder = checkoutBorder,
                        darkTheme = darkTheme,
                        onHoldOrder = { viewModel.setShowHoldOrderDialog(true) },
                        onPlaceOrder = { viewModel.setShowPaymentDialog(true) },
                        onQuantityChange = { id, delta -> viewModel.changeQuantity(id, delta) },
                        onSelectDiscount = { viewModel.selectDiscount(it) },
                        forceExpanded = false,
                        useBottomSheetStyle = true
                    )
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

@Composable
private fun DashboardHeaderIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
    ) {
        FluentIcon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            size = 20.dp
        )
    }
}

@Composable
private fun StorefrontCatalogPane(
    uiState: DashboardUiState,
    hazeState: HazeState,
    headerState: CollapsingHeaderState,
    searchQuery: String,
    isSearchExpanded: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit,
    onItemClick: (Item) -> Unit,
    modifier: Modifier = Modifier,
    compactGlows: Boolean = false,
    bottomContentPadding: Dp = 0.dp
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .liquidGlassSource(hazeState)
    ) {
        AdaptiveAmbientGlows(
            modifier = Modifier.fillMaxSize(),
            compactLayout = compactGlows
        )
        val isSearching = searchQuery.isNotEmpty()
        val searchResults = remember(searchQuery, uiState.allMenuItems) {
            val query = searchQuery.trim()
            if (query.isEmpty()) {
                emptyList()
            } else {
                uiState.allMenuItems.mapNotNull { it.toCatalogSearchHit(query) }
            }
        }
        val browseGroupedItems = remember(
            uiState.menuItems,
            uiState.selectedCategoryId,
            uiState.categories
        ) {
            if (uiState.menuItems.isEmpty()) {
                emptyList()
            } else {
                val categoryName = uiState.categories
                    .find { it.id == uiState.selectedCategoryId }
                    ?.name
                    .orEmpty()
                listOf(categoryName to uiState.menuItems)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (isSearchExpanded || isSearching) {
                GlassSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = "Search menu...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    onClose = {
                        onSearchQueryChange("")
                        onSearchExpandedChange(false)
                    }
                )
            }
            if (!isSearching) {
                CategorySelector(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategorySelected = onCategorySelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            val catalogListModifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .then(
                    if (isSearchExpanded || isSearching) {
                        Modifier
                    } else {
                        Modifier.collapsingNestedScroll(headerState)
                    }
                )

            LazyColumn(
                modifier = catalogListModifier,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = bottomContentPadding)
            ) {
                if (isSearching) {
                    if (searchResults.isEmpty()) {
                        item(key = "search_empty") {
                            Text(
                                text = "No menu items match your search.",
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    } else {
                        items(
                            items = searchResults.chunked(2),
                            key = { row -> "search_${row.joinToString("_") { it.item.id }}" }
                        ) { rowHits ->
                            CatalogSearchResultRow(
                                hits = rowHits,
                                uiState = uiState,
                                onItemClick = onItemClick
                            )
                        }
                    }
                } else {
                    if (browseGroupedItems.isEmpty()) {
                        item(key = "browse_empty") {
                            Text(
                                text = "No items in this category.",
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    } else {
                        browseGroupedItems.forEach { (categoryName, categoryItems) ->
                            items(
                                items = categoryItems.chunked(2),
                                key = { row -> "browse_${categoryName}_${row.joinToString("_") { it.id }}" }
                            ) { rowProducts ->
                                CatalogProductRow(
                                    products = rowProducts,
                                    uiState = uiState,
                                    onItemClick = onItemClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class CatalogSearchHit(
    val item: Item,
    val matchLabel: String?
)

@Composable
private fun CatalogSearchResultRow(
    hits: List<CatalogSearchHit>,
    uiState: DashboardUiState,
    onItemClick: (Item) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        hits.forEach { hit ->
            val itemMappings = uiState.recipeMappings.filter { it.menuItemId == hit.item.id }
            val isLowStock = if (itemMappings.isEmpty()) {
                false
            } else {
                itemMappings.any { mapping ->
                    val invItem = uiState.inventory.find { it.id == mapping.inventoryItemId }
                    invItem != null && invItem.currentStock <= invItem.reorderThreshold
                }
            }
            ItemCard(
                item = hit.item,
                isLowStock = isLowStock,
                searchMatchLabel = hit.matchLabel,
                onClick = { onItemClick(hit.item) },
                modifier = Modifier.weight(1f)
            )
        }
        if (hits.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CatalogProductRow(
    products: List<Item>,
    uiState: DashboardUiState,
    onItemClick: (Item) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        products.forEach { item ->
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
                onClick = { onItemClick(item) },
                modifier = Modifier.weight(1f)
            )
        }
        if (products.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun Item.toCatalogSearchHit(query: String): CatalogSearchHit? {
    val tokens = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (tokens.isEmpty()) return null

    val flavorMatch = flavors.firstOrNull { flavor ->
        val flavorText = flavor.substringAfter(": ").trim().ifEmpty { flavor }
        tokens.all { token ->
            flavor.contains(token, ignoreCase = true) ||
                flavorText.contains(token, ignoreCase = true)
        }
    }
    if (flavorMatch != null) {
        val label = flavorMatch.substringAfter(": ").trim().ifEmpty { flavorMatch }
        return CatalogSearchHit(this, label)
    }

    val variantMatch = variants.firstOrNull { variant ->
        tokens.all { token -> variant.name.contains(token, ignoreCase = true) }
    }?.name
    if (variantMatch != null) {
        return CatalogSearchHit(this, variantMatch)
    }

    if (tokens.all { token -> name.contains(token, ignoreCase = true) }) {
        return CatalogSearchHit(this, matchLabel = null)
    }

    return null
}

@Composable
private fun DashboardCheckoutPanel(
    uiState: DashboardUiState,
    cartItemCount: Int,
    isCartExpanded: Boolean,
    onCartExpandedChange: (Boolean) -> Unit,
    checkoutBorder: BorderStroke,
    darkTheme: Boolean,
    onHoldOrder: () -> Unit,
    onPlaceOrder: () -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onSelectDiscount: (DiscountStrategy) -> Unit,
    modifier: Modifier = Modifier,
    forceExpanded: Boolean = false,
    useBottomSheetStyle: Boolean = true
) {
    if (useBottomSheetStyle) {
        val cartBarShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        val labelColor = adaptiveGlassContentColor(darkTheme)
        val chevronRotation by animateFloatAsState(
            targetValue = if (isCartExpanded) 180f else 0f,
            animationSpec = iOSSpringSpec,
            label = "cartChevronRotation"
        )
        val performHaptic = rememberBionicHaptic()
        val toggleCart: () -> Unit = {
            performHaptic(BionicHaptic.Selection)
            onCartExpandedChange(!isCartExpanded)
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = iOSSpringSize)
                .clip(cartBarShape)
                .background(adaptiveGlassBrush(darkTheme), cartBarShape)
                .border(checkoutBorder.width, checkoutBorder.brush, cartBarShape)
                .shadow(8.dp, cartBarShape, clip = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = toggleCart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = cartItemCount,
                            transitionSpec = {
                                fadeIn(animationSpec = iOSSpringSpec) togetherWith fadeOut(animationSpec = iOSSpringSpec)
                            },
                            label = "cartCount"
                        ) { count ->
                            Text(
                                "Current Order ($count)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = labelColor
                            )
                        }
                        AnimatedVisibility(
                            visible = cartItemCount > 0,
                            enter = expandVertically(animationSpec = iOSSpringSize) + fadeIn(animationSpec = iOSSpringSpec),
                            exit = shrinkVertically(animationSpec = iOSSpringSize) + fadeOut(animationSpec = iOSSpringSpec)
                        ) {
                            Text(
                                "Total: ₱${String.format("%.0f", uiState.total)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onHoldOrder,
                            enabled = uiState.activeCart.isNotEmpty(),
                            modifier = Modifier.height(32.dp)
                        ) {
                            FluentIcon(
                                imageVector = FluentIcons.Pause,
                                contentDescription = null,
                                size = 14.dp,
                                useGlassGradient = false
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hold", fontSize = 12.sp)
                        }
                        IconButton(onClick = toggleCart) {
                            FluentIcon(
                                imageVector = FluentIcons.ChevronUp,
                                contentDescription = if (isCartExpanded) {
                                    "Collapse order panel"
                                } else {
                                    "Expand order panel"
                                },
                                useGlassGradient = false,
                                modifier = Modifier.graphicsLayer { rotationZ = chevronRotation }
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
                        DashboardCheckoutBody(
                            uiState = uiState,
                            onQuantityChange = onQuantityChange,
                            onSelectDiscount = onSelectDiscount,
                            onPlaceOrder = onPlaceOrder,
                            listMaxHeight = 220.dp
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Current Order ($cartItemCount)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(
                    onClick = onHoldOrder,
                    enabled = uiState.activeCart.isNotEmpty(),
                    modifier = Modifier.height(32.dp)
                ) {
                    FluentIcon(
                        imageVector = FluentIcons.Pause,
                        contentDescription = null,
                        size = 14.dp,
                        useGlassGradient = false
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hold", fontSize = 12.sp)
                }
            }
            DashboardCheckoutBody(
                uiState = uiState,
                onQuantityChange = onQuantityChange,
                onSelectDiscount = onSelectDiscount,
                onPlaceOrder = onPlaceOrder,
                listMaxHeight = null
            )
        }
    }
}

@Composable
private fun DashboardCheckoutBody(
    uiState: DashboardUiState,
    onQuantityChange: (String, Int) -> Unit,
    onSelectDiscount: (DiscountStrategy) -> Unit,
    onPlaceOrder: () -> Unit,
    listMaxHeight: Dp? = 150.dp
) {
    val bodyColor = adaptiveGlassContentColor()
    val mutedColor = adaptiveBodyMuted()
    Spacer(modifier = Modifier.height(8.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (listMaxHeight != null) {
                    Modifier.heightIn(max = listMaxHeight).verticalScroll(rememberScrollState())
                } else {
                    Modifier
                }
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (uiState.activeCart.isEmpty()) {
            Text(
                "No items yet",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = mutedColor,
                textAlign = TextAlign.Center
            )
        } else {
            uiState.activeCart.forEach { cartItem ->
                CartItemRow(
                    cartItem = cartItem,
                    onQuantityChange = onQuantityChange
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
                color = mutedColor
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
            Text("Total: ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = bodyColor)
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
        DiscountButton("None", uiState.selectedDiscountStrategy is NoDiscountStrategy, { onSelectDiscount(NoDiscountStrategy()) }, Modifier.weight(1f))
        DiscountButton("5%", uiState.selectedDiscountStrategy is FivePercentDiscountStrategy, { onSelectDiscount(FivePercentDiscountStrategy()) }, Modifier.weight(1f))
        DiscountButton("10%", uiState.selectedDiscountStrategy is PercentageDiscountStrategy && (uiState.selectedDiscountStrategy as PercentageDiscountStrategy).pct == 10.0, { onSelectDiscount(PercentageDiscountStrategy(10.0)) }, Modifier.weight(1f))
        DiscountButton("20%", uiState.selectedDiscountStrategy is PercentageDiscountStrategy && (uiState.selectedDiscountStrategy as PercentageDiscountStrategy).pct == 20.0, { onSelectDiscount(PercentageDiscountStrategy(20.0)) }, Modifier.weight(1f))
        DiscountButton("Free", uiState.selectedDiscountStrategy is FreeOrderDiscountStrategy, { onSelectDiscount(FreeOrderDiscountStrategy()) }, Modifier.weight(1.2f))
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
        onClick = onPlaceOrder,
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
            text = "Place Order",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
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
fun ItemCard(
    item: Item,
    isLowStock: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    searchMatchLabel: String? = null
) {
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
                        imageVector = FluentIcons.categoryIcon(item.categoryId),
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
                    if (!searchMatchLabel.isNullOrBlank()) {
                        Text(
                            text = searchMatchLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
    val lineColor = adaptiveGlassContentColor()
    ObsidianGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val variantFlavorText = if (cartItem.flavor.isNullOrBlank()) {
                    cartItem.variant.name
                } else {
                    "${cartItem.variant.name}/${cartItem.flavor.substringAfter(": ").trim()}"
                }
                Text(
                    "${cartItem.quantity}x ${cartItem.item.name} ($variantFlavorText) - ₱${String.format("%.0f", cartItem.totalPrice)}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = lineColor,
                    lineHeight = 16.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChange(cartItem.id, -1) }, modifier = Modifier.size(24.dp)) {
                    FluentIcon(imageVector = FluentIcons.Subtract, contentDescription = null, size = 14.dp, useGlassGradient = false)
                }
                IconButton(onClick = { onQuantityChange(cartItem.id, 1) }, modifier = Modifier.size(24.dp)) {
                    FluentIcon(imageVector = FluentIcons.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 14.dp)
                }
                IconButton(onClick = { onQuantityChange(cartItem.id, -cartItem.quantity) }, modifier = Modifier.size(24.dp)) {
                    FluentIcon(imageVector = FluentIcons.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, size = 14.dp)
                }
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

    AdaptiveGlassDialog(
        onDismissRequest = onDismiss,
        surfaceAlpha = 0.93f,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payment Checkout", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "₱${String.format("%.0f", finalTotal)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            PosPrimaryButton(
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
            ) {
                Text(
                    text = "Confirm & Pay",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        content = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Total Due",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CupertinoSegmentChip(
                        selected = paymentState.selectedTabIndex == 0,
                        onClick = { onPaymentStateChange(paymentState.copy(selectedTabIndex = 0)) },
                        label = "Cash",
                        modifier = Modifier.weight(1f)
                    )
                    CupertinoSegmentChip(
                        selected = paymentState.selectedTabIndex == 1,
                        onClick = { onPaymentStateChange(paymentState.copy(selectedTabIndex = 1)) },
                        label = "GCash",
                        modifier = Modifier.weight(1f)
                    )
                }
                if (isCash) {
                    OutlinedTextField(
                        value = paymentState.amountTenderedStr,
                        onValueChange = { onPaymentStateChange(paymentState.copy(amountTenderedStr = it)) },
                        label = { Text("Amount Tendered (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    AdaptiveGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Change Due", fontWeight = FontWeight.Medium)
                            Text(
                                if (changeDue >= 0) "₱${String.format("%.0f", changeDue)}" else "---",
                                fontWeight = FontWeight.Bold,
                                color = if (changeDue >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
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
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    )
}

private enum class ProductConfigStep {
    FlavorGroup,
    Flavor,
    Size
}

private fun itemHasGroupedFlavors(item: Item): Boolean =
    item.flavors.any { it.contains(":") }

private fun itemFlavorGroups(item: Item): Map<String, List<String>> =
    item.flavors.groupBy { flavor ->
        if (flavor.contains(":")) flavor.substringBefore(":").trim() else "Flavors"
    }

private fun initialProductConfigStep(item: Item): ProductConfigStep = when {
    itemHasGroupedFlavors(item) -> ProductConfigStep.FlavorGroup
    item.flavors.isNotEmpty() -> ProductConfigStep.Flavor
    else -> ProductConfigStep.Size
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductConfigBottomSheet(item: Item, onDismiss: () -> Unit, onAddToCart: (Variant, String?) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentStep by remember(item.id) { mutableStateOf(initialProductConfigStep(item)) }
    var selectedFlavorGroup by remember(item.id) { mutableStateOf<String?>(null) }
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
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    liquidSwipeTransition(forward = targetState.ordinal > initialState.ordinal)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds(),
                label = "ProductConfigStep"
            ) { step ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    when (step) {
                        ProductConfigStep.FlavorGroup -> {
                            ProductConfigStepHeader(
                                title = item.name,
                                subtitle = "Choose a style"
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemFlavorGroups(item).forEach { (group, _) ->
                                    key(group) {
                                        FlavorOptionRow(
                                            label = group,
                                            isSelected = selectedFlavorGroup == group,
                                            onSelect = {
                                                selectedFlavorGroup = group
                                                selectedFlavor = null
                                                currentStep = ProductConfigStep.Flavor
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        ProductConfigStep.Flavor -> {
                            val flavorsForStep = if (selectedFlavorGroup != null) {
                                itemFlavorGroups(item)[selectedFlavorGroup].orEmpty()
                            } else {
                                item.flavors
                            }
                            ProductConfigStepHeader(
                                title = item.name,
                                subtitle = selectedFlavorGroup ?: "Choose a flavor",
                                onBack = {
                                    if (itemHasGroupedFlavors(item)) {
                                        selectedFlavor = null
                                        selectedFlavorGroup = null
                                        currentStep = ProductConfigStep.FlavorGroup
                                    } else {
                                        onDismiss()
                                    }
                                }
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                flavorsForStep.forEach { flavor ->
                                    key(flavor) {
                                        FlavorOptionRow(
                                            label = flavor.substringAfter(": ").trim(),
                                            isSelected = selectedFlavor == flavor,
                                            onSelect = {
                                                selectedFlavor = flavor
                                                currentStep = ProductConfigStep.Size
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tap a flavor to continue",
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        ProductConfigStep.Size -> {
                            ProductConfigStepHeader(
                                title = item.name,
                                subtitle = selectedFlavor?.substringAfter(": ")?.trim()
                                    ?: if (item.flavors.isEmpty()) "Choose an option" else "Choose a size",
                                onBack = {
                                    if (item.flavors.isNotEmpty()) {
                                        selectedFlavor = null
                                        currentStep = ProductConfigStep.Flavor
                                    } else {
                                        onDismiss()
                                    }
                                }
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
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
                            if (hasComboDescriptions) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val panelTextColor = adaptiveGlassContentColor()
                                AdaptiveGlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            "Included in this combo:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = panelTextColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = selectedVariant.description ?: "Select an option to view combo details.",
                                            fontSize = 12.sp,
                                            color = panelTextColor,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Crossfade(
                targetState = currentStep == ProductConfigStep.Size,
                animationSpec = tween(durationMillis = 140),
                label = "ProductConfigFooter"
            ) { showCheckoutFooter ->
                if (showCheckoutFooter) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
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
                            PosPrimaryButton(
                                onClick = { onAddToCart(selectedVariant, selectedFlavor) },
                                enabled = !(item.flavors.isNotEmpty() && selectedFlavor == null),
                                modifier = Modifier.defaultMinSize(minWidth = 0.dp)
                            ) {
                                PosButtonIconLabel(
                                    icon = {
                                        FluentIcon(
                                            imageVector = FluentIcons.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            size = 24.dp
                                        )
                                    },
                                    label = "Add to Order",
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductConfigStepHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                FluentIcon(
                    imageVector = FluentIcons.ArrowLeft,
                    contentDescription = "Back",
                    useGlassGradient = false
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (onBack == null) 0.dp else 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FlavorOptionRow(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val labelColor = if (isSelected) {
        if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    } else {
        adaptiveBodyMuted(darkTheme)
    }
    val optionShape = RoundedCornerShape(16.dp)
    val performHaptic = rememberBionicHaptic()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(optionShape)
            .then(
                if (isSelected) {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                        shape = optionShape
                    )
                } else {
                    Modifier.background(
                        brush = adaptiveGlassBrush(darkTheme),
                        shape = optionShape
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else if (darkTheme) {
                    Color.White.copy(alpha = 0.05f)
                } else {
                    Color.Black.copy(alpha = 0.05f)
                },
                shape = optionShape
            )
            .clickable {
                performHaptic(BionicHaptic.Selection)
                onSelect()
            }
    ) {
        Text(
            text = label,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = labelColor
        )
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
    val optionShape = RoundedCornerShape(16.dp)
    val performHaptic = rememberBionicHaptic()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(optionShape)
            .then(
                if (isSelected) {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                        shape = optionShape
                    )
                } else {
                    Modifier.background(
                        brush = adaptiveGlassBrush(darkTheme),
                        shape = optionShape
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else if (darkTheme) {
                    Color.White.copy(alpha = 0.05f)
                } else {
                    Color.Black.copy(alpha = 0.05f)
                },
                shape = optionShape
            )
            .clickable {
                performHaptic(BionicHaptic.Selection)
                onSelect()
            }
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
    val panelTextColor = adaptiveGlassContentColor()
    val mutedTextColor = adaptiveBodyMuted()
    AdaptiveGlassDialog(
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        content = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                if (heldQueues.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No held orders in queue.", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        items(heldQueues) { queue ->
                            AdaptiveGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val labelText = queue.tableLabel?.let { " [$it]" } ?: ""
                                        Text(
                                            "Queue #${queue.id}$labelText",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = panelTextColor
                                        )
                                        val timeStr = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                                            .format(java.util.Date(queue.timestamp))
                                        Text(
                                            "Held at: $timeStr",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "${queue.items.sumOf { it.quantity }} items • ₱${String.format("%.0f", queue.items.sumOf { it.totalPrice })}",
                                            fontSize = 12.sp,
                                            color = mutedTextColor
                                        )
                                    }
                                    PosPrimaryButton(
                                        onClick = { onResume(queue.id) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Resume",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun HoldOrderDialog(onHold: (String?) -> Unit, onDismiss: () -> Unit) {
    var tableLabel by remember { mutableStateOf("") }

    AdaptiveGlassDialog(
        onDismissRequest = onDismiss,
        surfaceAlpha = 0.93f,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FluentIcon(
                    imageVector = FluentIcons.Pause,
                    contentDescription = null,
                    size = 20.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hold Order", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            PosPrimaryButton(onClick = { onHold(tableLabel.takeIf { it.isNotBlank() }) }) {
                PosButtonIconLabel(
                    icon = {
                        FluentIcon(
                            imageVector = FluentIcons.Pause,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            size = 18.dp
                        )
                    },
                    label = "Hold Order",
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Park this order and resume it later from the queue.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = tableLabel,
                    onValueChange = { tableLabel = it },
                    label = { Text("Table / Label (Optional)") },
                    placeholder = { Text("e.g. Table 3, Take-out #5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun AddExpenseDialog(onSave: (String, Double, String) -> Unit, onDismiss: () -> Unit) {
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var recordedBy by remember { mutableStateOf("") }
    val amount = amountStr.toDoubleOrNull()
    val isReady = description.isNotBlank() && amount != null && amount > 0 && recordedBy.isNotBlank()

    AdaptiveGlassDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Expense (from Cash Drawer)", fontWeight = FontWeight.Bold) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            Button(
                onClick = { if (isReady) onSave(description, amount!!, recordedBy) },
                enabled = isReady
            ) { Text("Save Expense") }
        },
        content = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (e.g. Supplies: Ice)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Amount (₱)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = recordedBy, onValueChange = { recordedBy = it }, label = { Text("Recorded By (Name)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}



