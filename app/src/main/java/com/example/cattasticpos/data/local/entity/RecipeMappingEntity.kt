package com.example.cattasticpos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_mappings")
data class RecipeMappingEntity(
    @PrimaryKey val id: String,
    val menuItemId: String,
    val variantName: String?,
    val inventoryItemId: String,
    val deductionQuantity: Double
)
