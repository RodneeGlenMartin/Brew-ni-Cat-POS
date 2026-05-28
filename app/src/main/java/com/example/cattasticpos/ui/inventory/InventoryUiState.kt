package com.example.cattasticpos.ui.inventory

import com.example.cattasticpos.domain.model.InventoryItem
import com.example.cattasticpos.domain.model.RecipeMapping
import com.example.cattasticpos.domain.model.Item

data class InventoryUiState(
    val inventoryItems: List<InventoryItem> = emptyList(),
    val menuItems: List<Item> = emptyList(),
    val selectedMenuItemId: String? = null,
    val selectedVariantName: String? = null,
    val currentRecipeMappings: List<RecipeMapping> = emptyList(),
    val showAddRawMaterialDialog: Boolean = false,
    val showLinkIngredientDialog: Boolean = false
)
