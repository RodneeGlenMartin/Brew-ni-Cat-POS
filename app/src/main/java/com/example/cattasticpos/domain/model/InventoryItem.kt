package com.example.cattasticpos.domain.model

data class InventoryItem(
    val id: String,
    val itemName: String,
    val unit: String,
    val currentStock: Double,
    val reorderThreshold: Double
)
