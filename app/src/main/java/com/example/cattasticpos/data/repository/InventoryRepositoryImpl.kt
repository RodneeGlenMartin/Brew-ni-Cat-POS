package com.example.cattasticpos.data.repository

import com.example.cattasticpos.data.local.dao.InventoryDao
import com.example.cattasticpos.data.local.entity.InventoryEntity
import com.example.cattasticpos.domain.model.InventoryItem
import com.example.cattasticpos.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepositoryImpl(
    private val inventoryDao: InventoryDao
) : InventoryRepository {

    override fun getAllInventory(): Flow<List<InventoryItem>> {
        return inventoryDao.getAllInventory().map { entities ->
            entities.map { entity ->
                InventoryItem(
                    id = entity.id,
                    itemName = entity.itemName,
                    unit = entity.unit,
                    currentStock = entity.currentStock,
                    reorderThreshold = entity.reorderThreshold
                )
            }
        }
    }

    override suspend fun insertInventoryItems(items: List<InventoryItem>) {
        val entities = items.map { item ->
            InventoryEntity(
                id = item.id,
                itemName = item.itemName,
                unit = item.unit,
                currentStock = item.currentStock,
                reorderThreshold = item.reorderThreshold
            )
        }
        inventoryDao.insertInventoryItems(entities)
    }

    override suspend fun decrementStock(inventoryId: String, amount: Double) {
        inventoryDao.decrementStock(inventoryId, amount)
    }

    override suspend fun restockItem(itemId: String, addedAmount: Double) {
        inventoryDao.restockItem(itemId, addedAmount)
    }
}
