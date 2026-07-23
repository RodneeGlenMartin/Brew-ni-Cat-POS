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
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun getInventoryItemById(itemId: String): InventoryItem? {
        return inventoryDao.getInventoryItemById(itemId)?.toDomain()
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

    override suspend fun updateInventoryItem(item: InventoryItem) {
        inventoryDao.updateInventoryItem(
            InventoryEntity(
                id = item.id,
                itemName = item.itemName,
                unit = item.unit,
                currentStock = item.currentStock,
                reorderThreshold = item.reorderThreshold
            )
        )
    }

    override suspend fun decrementStock(inventoryId: String, amount: Double) {
        inventoryDao.decrementStock(inventoryId, amount)
    }

    override suspend fun restockItem(itemId: String, addedAmount: Double) {
        inventoryDao.restockItem(itemId, addedAmount)
    }

    override suspend fun deleteInventoryItem(itemId: String) {
        inventoryDao.deleteInventoryItem(itemId)
    }

    private fun InventoryEntity.toDomain() = InventoryItem(
        id = id,
        itemName = itemName,
        unit = unit,
        currentStock = currentStock,
        reorderThreshold = reorderThreshold
    )
}
