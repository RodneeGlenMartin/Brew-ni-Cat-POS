package com.example.cattasticpos.domain.repository

import com.example.cattasticpos.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getAllInventory(): Flow<List<InventoryEntity>>
    suspend fun insertInventoryItems(items: List<InventoryEntity>)
    suspend fun decrementStock(inventoryId: String, amount: Int)
    suspend fun restockItem(itemId: String, addedAmount: Int)
}
