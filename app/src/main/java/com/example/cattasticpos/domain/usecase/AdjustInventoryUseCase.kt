package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.repository.InventoryRepository

class AdjustInventoryUseCase(
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(itemId: String, reduceAmount: Double) {
        if (reduceAmount <= 0) return
        val item = inventoryRepository.getInventoryItemById(itemId) ?: return
        val newStock = (item.currentStock - reduceAmount).coerceAtLeast(0.0)
        inventoryRepository.updateInventoryItem(item.copy(currentStock = newStock))
    }
}
