package com.example.cattasticpos.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cattasticpos.domain.model.InventoryItem
import com.example.cattasticpos.data.local.entity.RecipeMappingEntity
import com.example.cattasticpos.domain.model.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📦 Inventory & Recipes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
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
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(onClick = { viewModel.setShowAddRawMaterialDialog(true) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Raw Material")
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
                RawMaterialsTab(
                    inventoryItems = uiState.inventoryItems,
                    onRestock = { itemId, qty -> viewModel.restockItem(itemId, qty) }
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

@Composable
fun InventoryStockTab(
    inventoryItems: List<InventoryItem>,
    onRestock: (String, Double) -> Unit
) {
    if (inventoryItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No inventory tracked. Click + to add.", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(inventoryItems, key = { it.id }) { item ->
                var amountStr by remember { mutableStateOf("") }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Reorder at: ${item.reorderThreshold} ${item.unit}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            Text(
                                text = "Stock: ${item.currentStock} ${item.unit}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (item.currentStock <= item.reorderThreshold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
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
                            Button(
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
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restock")
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
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        var menuDropdownExpanded by remember { mutableStateOf(false) }
        var variantDropdownExpanded by remember { mutableStateOf(false) }

        val selectedMenu = uiState.menuItems.find { it.id == uiState.selectedMenuItemId }
        val variants = remember(selectedMenu) {
            selectedMenu?.variants?.map { it.name } ?: emptyList()
        }

        ExposedDropdownMenuBox(
            expanded = menuDropdownExpanded,
            onExpandedChange = { menuDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedMenu?.name ?: "Select Menu Item",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuDropdownExpanded) }
            )
            ExposedDropdownMenu(
                expanded = menuDropdownExpanded,
                onDismissRequest = { menuDropdownExpanded = false }
            ) {
                uiState.menuItems.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            onSelectMenu(item.id)
                            menuDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedMenu != null) {
            ExposedDropdownMenuBox(
                expanded = variantDropdownExpanded,
                onExpandedChange = { variantDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.selectedVariantName ?: "All Variants (Base Item)",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = variantDropdownExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = variantDropdownExpanded,
                    onDismissRequest = { variantDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Variants (Base Item)", fontWeight = FontWeight.Bold) },
                        onClick = {
                            onSelectVariant(null)
                            variantDropdownExpanded = false
                        }
                    )
                    variants.forEach { vName ->
                        DropdownMenuItem(
                            text = { Text(vName) },
                            onClick = {
                                onSelectVariant(vName)
                                variantDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recipe BOM Mappings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(onClick = onOpenLinkDialog) {
                    Text("+ Link Ingredient")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            val filteredMappings = uiState.currentRecipeMappings.filter { it.variantName == uiState.selectedVariantName }
            if (filteredMappings.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No ingredients mapped.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredMappings) { mapping ->
                        val inventoryItem = uiState.inventoryItems.find { it.id == mapping.inventoryItemId }
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(inventoryItem?.itemName ?: "Unknown Item", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Deduct: ${mapping.deductionQuantity} ${inventoryItem?.unit ?: ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onRemoveMapping(mapping) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Raw Material") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, singleLine = true)
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (e.g. pcs, grams)") }, singleLine = true)
                OutlinedTextField(value = stockStr, onValueChange = { stockStr = it }, label = { Text("Starting Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = threshStr, onValueChange = { threshStr = it }, label = { Text("Reorder Threshold") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
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
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
