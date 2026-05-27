package com.example.cattasticpos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey val id: String,
    val itemName: String,
    val unit: String,
    val currentStock: Int,
    val reorderThreshold: Int
)
