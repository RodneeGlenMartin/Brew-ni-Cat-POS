package com.example.cattasticpos.domain.model

data class RecipeMapping(
    val id: String,
    val menuItemId: String,
    val variantName: String?,
    val inventoryItemId: String,
    val deductionQuantity: Double
)
