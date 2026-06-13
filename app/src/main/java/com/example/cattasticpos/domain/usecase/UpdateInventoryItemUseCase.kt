package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.InventoryItem
import com.example.cattasticpos.domain.repository.InventoryRepository

class UpdateInventoryItemUseCase(
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(
        itemId: String,
        itemName: String,
        unit: String,
        currentStock: Double,
        reorderThreshold: Double
    ) {
        if (itemName.isBlank() || unit.isBlank()) return
        val item = inventoryRepository.getInventoryItemById(itemId) ?: return
        inventoryRepository.updateInventoryItem(
            item.copy(
                itemName = itemName.trim(),
                unit = unit.trim(),
                currentStock = currentStock.coerceAtLeast(0.0),
                reorderThreshold = reorderThreshold.coerceAtLeast(0.0)
            )
        )
    }
}
