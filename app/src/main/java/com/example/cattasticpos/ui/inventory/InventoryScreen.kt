package com.example.cattasticpos.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import com.example.cattasticpos.ui.adaptive.AdaptiveFloatingActionButton
import com.example.cattasticpos.ui.adaptive.AdaptiveScaffold
import com.example.cattasticpos.ui.adaptive.AdaptiveTopAppBar
import com.example.cattasticpos.ui.adaptive.CupertinoSection
import com.example.cattasticpos.ui.adaptive.LocalCupertinoColors
import com.example.cattasticpos.ui.adaptive.adaptiveNestedScroll
import com.example.cattasticpos.ui.adaptive.rememberAdaptiveTopBarScrollBehavior
import com.example.cattasticpos.ui.components.unstyled.PosPrimaryButton
import com.example.cattasticpos.ui.theme.AdaptiveGlassCard
import com.example.cattasticpos.ui.theme.AdaptiveGlassDialog
import com.example.cattasticpos.ui.theme.adaptiveBodyMuted
import com.example.cattasticpos.ui.theme.adaptiveGlassBrush
import com.example.cattasticpos.ui.icons.FluentIcon
import com.example.cattasticpos.ui.icons.FluentIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cattasticpos.domain.model.InventoryItem
import com.example.cattasticpos.domain.model.RecipeMapping
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.usecase.RecipeDeductionResolver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }

    val scrollBehavior = rememberAdaptiveTopBarScrollBehavior()
    val cupertino = LocalCupertinoColors.current

    AdaptiveScaffold(
        topBar = {
            AdaptiveTopAppBar(
                title = "Inventory & Recipes",
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                            FluentIcon(
                                imageVector = FluentIcons.ArrowLeft,
                                contentDescription = "Go Back",
                                tint = cupertino.accent,
                                size = 24.dp
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                AdaptiveFloatingActionButton(onClick = { viewModel.setShowAddRawMaterialDialog(true) }) {
                    FluentIcon(
                        imageVector = FluentIcons.Add,
                        contentDescription = "Add Raw Material",
                        tint = cupertino.onAccent,
                        size = 24.dp
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Raw Materials", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Product Recipes", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTabIndex == 0) {
                InventoryStockTab(
                    inventoryItems = uiState.inventoryItems,
                    scrollBehavior = scrollBehavior,
                    onRestock = { itemId, qty -> viewModel.restockItem(itemId, qty) },
                    onDelete = { viewModel.deleteInventoryItem(it) }
                )
            } else {
                ProductRecipesTab(
                    uiState = uiState,
                    onSelectMenu = { viewModel.selectMenuItem(it) },
                    onSelectVariant = { viewModel.selectVariant(it) },
                    onOpenLinkDialog = { viewModel.setShowLinkIngredientDialog(true) },
                    onRemoveMapping = { viewModel.removeMapping(it) }
                )
            }
        }

        if (uiState.showAddRawMaterialDialog) {
            AddRawMaterialDialog(
                onSave = { name, unit, stock, thresh -> viewModel.addNewRawMaterial(name, unit, stock, thresh) },
                onDismiss = { viewModel.setShowAddRawMaterialDialog(false) }
            )
        }

        if (uiState.showLinkIngredientDialog) {
            LinkIngredientDialog(
                inventoryItems = uiState.inventoryItems,
                onSave = { invId, qty -> 
                    viewModel.linkIngredient(invId, qty)
                    viewModel.setShowLinkIngredientDialog(false)
                },
                onDismiss = { viewModel.setShowLinkIngredientDialog(false) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryStockTab(
    inventoryItems: List<InventoryItem>,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior? = null,
    onRestock: (String, Double) -> Unit,
    onDelete: (String) -> Unit
) {
    if (inventoryItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No inventory tracked. Click + to add.", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp).adaptiveNestedScroll(scrollBehavior),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(inventoryItems, key = { it.id }) { item ->
                var amountStr by remember { mutableStateOf("") }

                CupertinoSection {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Reorder at: ${item.reorderThreshold} ${item.unit}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Stock: ${item.currentStock} ${item.unit}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (item.currentStock <= item.reorderThreshold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { onDelete(item.id) }) {
                                    FluentIcon(
                                        imageVector = FluentIcons.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        size = 24.dp
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = amountStr,
                                onValueChange = { amountStr = it },
                                label = { Text("Amount") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(100.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PosPrimaryButton(
                                onClick = {
                                    val amt = amountStr.toDoubleOrNull()
                                    if (amt != null && amt > 0) {
                                        onRestock(item.id, amt)
                                        amountStr = ""
                                    }
                                },
                                enabled = amountStr.toDoubleOrNull() != null && (amountStr.toDoubleOrNull() ?: 0.0) > 0,
                                modifier = Modifier.height(50.dp)
                            ) {
                                FluentIcon(
                                    imageVector = FluentIcons.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    size = 16.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Restock",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductRecipesTab(
    uiState: InventoryUiState,
    onSelectMenu: (String) -> Unit,
    onSelectVariant: (String?) -> Unit,
    onOpenLinkDialog: () -> Unit,
    onRemoveMapping: (RecipeMapping) -> Unit
) {
    var menuDropdownExpanded by remember { mutableStateOf(false) }

    val selectedMenu = uiState.menuItems.find { it.id == uiState.selectedMenuItemId }
    val recipeTargetGroups = remember(selectedMenu) {
        selectedMenu?.recipeTargetGroups() ?: emptyList()
    }
    val filteredMappings = remember(uiState.currentRecipeMappings, uiState.selectedVariantName) {
        RecipeDeductionResolver.forRecipeEditorTarget(
            uiState.currentRecipeMappings,
            uiState.selectedVariantName
        )
    }
    val selectedTargetLabel = remember(uiState.selectedVariantName) {
        uiState.selectedVariantName?.substringAfter(": ")?.trim()
            ?.ifEmpty { uiState.selectedVariantName }
            ?: "All Variants"
    }
    val darkTheme = isSystemInDarkTheme()
    val menuDropdownShape = RoundedCornerShape(12.dp)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AdaptiveGlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = menuDropdownExpanded,
                        onExpandedChange = { menuDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedMenu?.name ?: "Select Menu Item",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuDropdownExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unfocusedBorderColor = if (darkTheme) {
                                    Color.White.copy(alpha = 0.12f)
                                } else {
                                    Color.Black.copy(alpha = 0.08f)
                                }
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = menuDropdownExpanded,
                            onDismissRequest = { menuDropdownExpanded = false },
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .clip(menuDropdownShape)
                                .background(adaptiveGlassBrush(darkTheme))
                                .border(
                                    width = 1.dp,
                                    color = if (darkTheme) {
                                        Color.White.copy(alpha = 0.08f)
                                    } else {
                                        Color.Black.copy(alpha = 0.05f)
                                    },
                                    shape = menuDropdownShape
                                )
                        ) {
                            uiState.menuItems.forEach { menuItem ->
                                DropdownMenuItem(
                                    text = { Text(menuItem.name) },
                                    onClick = {
                                        onSelectMenu(menuItem.id)
                                        menuDropdownExpanded = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.background(Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedMenu != null) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                AdaptiveGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Recipe Target",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        InventoryVariantTargetRow(
                            label = "All Variants (Base Item)",
                            isSelected = uiState.selectedVariantName == null,
                            onClick = { onSelectVariant(null) }
                        )
                        recipeTargetGroups.forEach { (groupLabel, targets) ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = groupLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            targets.forEach { targetName ->
                                InventoryVariantTargetRow(
                                    label = targetName.substringAfter(": ").trim().ifEmpty { targetName },
                                    isSelected = uiState.selectedVariantName == targetName,
                                    onClick = { onSelectVariant(targetName) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Recipe BOM Mappings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Target: $selectedTargetLabel",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Button(onClick = onOpenLinkDialog) {
                        Text("+ Link Ingredient")
                    }
                }
            }

            if (filteredMappings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.selectedVariantName == null) {
                                "No base ingredients mapped."
                            } else {
                                "No ingredients mapped for $selectedTargetLabel."
                            },
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                items(filteredMappings, key = { it.id }) { mapping ->
                    val inventoryItem = uiState.inventoryItems.find { it.id == mapping.inventoryItemId }
                    val isInheritedBase = mapping.variantName == null && uiState.selectedVariantName != null
                    AdaptiveGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    inventoryItem?.itemName ?: "Unknown Item",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Deduct: ${mapping.deductionQuantity} ${inventoryItem?.unit ?: ""}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (isInheritedBase) {
                                    Text(
                                        text = "Applies to all sizes & flavors",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            if (!isInheritedBase) {
                                IconButton(onClick = { onRemoveMapping(mapping) }) {
                                    FluentIcon(
                                        imageVector = FluentIcons.Delete,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error,
                                        size = 24.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryVariantTargetRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val optionShape = RoundedCornerShape(12.dp)
    val labelColor = if (isSelected) {
        if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    } else {
        adaptiveBodyMuted(darkTheme)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
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
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
            color = labelColor
        )
    }
}

@Composable
fun AddRawMaterialDialog(
    onSave: (String, String, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }
    var threshStr by remember { mutableStateOf("") }

    AdaptiveGlassDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Raw Material", fontWeight = FontWeight.Bold) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            Button(onClick = {
                val stock = stockStr.toDoubleOrNull() ?: 0.0
                val thresh = threshStr.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && unit.isNotBlank()) {
                    onSave(name, unit, stock, thresh)
                }
            }) {
                Text("Save")
            }
        },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (e.g. pcs, grams)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stockStr, onValueChange = { stockStr = it }, label = { Text("Starting Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = threshStr, onValueChange = { threshStr = it }, label = { Text("Reorder Threshold") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkIngredientDialog(
    inventoryItems: List<InventoryItem>,
    onSave: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var qtyStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Ingredient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedItem?.itemName ?: "Select Raw Material",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        inventoryItems.forEach { item ->
                            DropdownMenuItem(text = { Text(item.itemName) }, onClick = { selectedItem = item; expanded = false })
                        }
                    }
                }

                if (selectedItem != null) {
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Deduct Quantity (${selectedItem!!.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val qty = qtyStr.toDoubleOrNull()
                if (selectedItem != null && qty != null && qty > 0) {
                    onSave(selectedItem!!.id, qty)
                }
            }) {
                Text("Link")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
