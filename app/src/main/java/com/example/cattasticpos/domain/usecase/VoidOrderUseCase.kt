package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.VoidRecord
import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.domain.repository.OrderRepository
import com.example.cattasticpos.domain.repository.RecipeRepository
import com.example.cattasticpos.domain.repository.TransactionProvider
import com.example.cattasticpos.domain.repository.VoidRepository
import java.util.UUID

class VoidOrderUseCase(
    private val orderRepository: OrderRepository,
    private val voidRepository: VoidRepository,
    private val recipeRepository: RecipeRepository,
    private val inventoryRepository: InventoryRepository,
    private val transactionProvider: TransactionProvider
) {
    suspend operator fun invoke(orderId: String, reason: String, cashierId: String?): Result<VoidRecord> {
        val order = orderRepository.getOrderById(orderId)
            ?: return Result.failure(IllegalArgumentException("Order not found"))

        return try {
            val voidRecord = VoidRecord(
                id = UUID.randomUUID().toString(),
                orderId = order.id,
                reason = reason,
                timestamp = System.currentTimeMillis(),
                cashierId = cashierId ?: order.cashierId,
                orderTotal = order.total
            )
            transactionProvider.runAsTransaction {
                InventoryRestorationHelper.restoreForOrderItems(
                    order.items,
                    recipeRepository,
                    inventoryRepository
                )
                voidRepository.saveVoidRecord(voidRecord)
                orderRepository.deleteOrder(order.id)
            }
            Result.success(voidRecord)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
