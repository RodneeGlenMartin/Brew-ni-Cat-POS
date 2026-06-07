package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.Order
import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.domain.repository.OrderRepository
import com.example.cattasticpos.domain.repository.RecipeRepository
import com.example.cattasticpos.domain.repository.TransactionProvider
import com.example.cattasticpos.domain.strategy.DiscountStrategy

class UpdateOrderUseCase(
    private val orderRepository: OrderRepository,
    private val inventoryRepository: InventoryRepository,
    private val recipeRepository: RecipeRepository,
    private val transactionProvider: TransactionProvider,
    private val calculateCartUseCase: CalculateCartUseCase = CalculateCartUseCase()
) {
    suspend operator fun invoke(
        orderId: Long,
        cartItems: List<CartItem>,
        discountStrategy: DiscountStrategy
    ): Result<Order> {
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("Receipt must have at least one item"))
        }

        val existing = orderRepository.getOrderById(orderId)
            ?: return Result.failure(IllegalArgumentException("Order not found"))

        val calculation = calculateCartUseCase(cartItems, discountStrategy)
        val updatedOrder = existing.copy(
            subtotal = calculation.subtotal,
            discountDeduction = calculation.discountDeduction,
            discountLabel = calculation.discountLabel,
            total = calculation.total,
            items = OrderCartMapper.cartItemsToOrderItems(orderId, cartItems)
        )

        return try {
            val persisted = transactionProvider.runAsTransaction {
                InventoryRestorationHelper.restoreForOrderItems(
                    existing.items,
                    recipeRepository,
                    inventoryRepository
                )
                val saved = orderRepository.updateOrder(updatedOrder)
                InventoryRestorationHelper.deductForCartItems(
                    cartItems,
                    recipeRepository,
                    inventoryRepository
                )
                saved
            }
            Result.success(persisted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
