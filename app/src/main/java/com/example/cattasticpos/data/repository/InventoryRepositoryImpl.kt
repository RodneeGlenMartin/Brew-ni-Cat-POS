package com.example.cattasticpos.data.repository

import com.example.cattasticpos.data.local.dao.InventoryDao
import com.example.cattasticpos.data.local.entity.InventoryEntity
import com.example.cattasticpos.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow

class InventoryRepositoryImpl(
    private val inventoryDao: InventoryDao
) : InventoryRepository {

    override fun getAllInventory(): Flow<List<InventoryEntity>> {
        return inventoryDao.getAllInventory()
    }

    override suspend fun insertInventoryItems(items: List<InventoryEntity>) {
        inventoryDao.insertInventoryItems(items)
    }

    override suspend fun decrementStock(inventoryId: String, amount: Int) {
        inventoryDao.decrementStock(inventoryId, amount)
    }

    override suspend fun restockItem(itemId: String, addedAmount: Int) {
        inventoryDao.restockItem(itemId, addedAmount)
    }
}
