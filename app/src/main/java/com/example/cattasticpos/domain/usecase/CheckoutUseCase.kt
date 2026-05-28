package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.domain.repository.RecipeRepository
import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.Order
import com.example.cattasticpos.domain.model.OrderItem
import com.example.cattasticpos.domain.repository.OrderRepository
import com.example.cattasticpos.domain.strategy.DiscountStrategy
import java.util.UUID

class CheckoutUseCase(
    private val orderRepository: OrderRepository,
    private val inventoryRepository: InventoryRepository,
    private val recipeRepository: RecipeRepository,
    private val calculateCartUseCase: CalculateCartUseCase = CalculateCartUseCase()
) {
    suspend operator fun invoke(
        items: List<CartItem>, 
        strategy: DiscountStrategy,
        paymentMethod: String,
        paymentReference: String?
    ): Result<Order> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cart is empty"))
        }
        val calculation = calculateCartUseCase(items, strategy)
        val orderId = UUID.randomUUID().toString()
        val orderItems = items.map { cartItem ->
            OrderItem(
                id = 0L,
                orderId = orderId,
                itemId = cartItem.item.id,
                itemName = cartItem.item.name,
                variantId = cartItem.variant.id,
                variantName = cartItem.variant.name,
                flavor = cartItem.flavor,
                quantity = cartItem.quantity,
                unitPrice = cartItem.unitPrice,
                totalPrice = cartItem.totalPrice
            )
        }
        val order = Order(
            id = orderId,
            timestamp = System.currentTimeMillis(),
            subtotal = calculation.subtotal,
            discountDeduction = calculation.discountDeduction,
            discountLabel = calculation.discountLabel,
            total = calculation.total,
            paymentMethod = paymentMethod,
            paymentReference = paymentReference,
            items = orderItems
        )
        return try {
            orderRepository.saveOrder(order)
            
            // Deduct inventory dynamically using Recipe Mappings
            items.forEach { cartItem ->
                val qty = cartItem.quantity
                val mappings = recipeRepository.getMappingsForCheckout(cartItem.item.id, cartItem.variant.name)
                mappings.forEach { mapping ->
                    val totalDeduction = mapping.deductionQuantity * qty
                    if (totalDeduction > 0) {
                        inventoryRepository.decrementStock(mapping.inventoryItemId, totalDeduction)
                    }
                }
            }
            
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
