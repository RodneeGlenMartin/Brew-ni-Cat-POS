package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.repository.InventoryRepository

class RestockItemUseCase(
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(itemId: String, addedAmount: Double) {
        if (addedAmount > 0) {
            inventoryRepository.restockItem(itemId, addedAmount)
        }
    }
}
