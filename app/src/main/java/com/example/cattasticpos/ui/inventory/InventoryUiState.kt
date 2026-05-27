package com.example.cattasticpos.ui.inventory

import com.example.cattasticpos.data.local.entity.InventoryEntity
import com.example.cattasticpos.data.local.entity.RecipeMappingEntity
import com.example.cattasticpos.domain.model.Item

data class InventoryUiState(
    val inventoryItems: List<InventoryEntity> = emptyList(),
    val menuItems: List<Item> = emptyList(),
    val selectedMenuItemId: String? = null,
    val selectedVariantName: String? = null,
    val currentRecipeMappings: List<RecipeMappingEntity> = emptyList(),
    val showAddRawMaterialDialog: Boolean = false,
    val showLinkIngredientDialog: Boolean = false
)
