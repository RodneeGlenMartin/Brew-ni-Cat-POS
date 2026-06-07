package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.domain.repository.RecipeRepository
import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.Order
import com.example.cattasticpos.domain.model.OrderItem
import com.example.cattasticpos.domain.repository.OrderRepository
import com.example.cattasticpos.domain.repository.TransactionProvider
import com.example.cattasticpos.domain.strategy.DiscountStrategy
import java.util.UUID

class CheckoutUseCase(
    private val orderRepository: OrderRepository,
    private val inventoryRepository: InventoryRepository,
    private val recipeRepository: RecipeRepository,
    private val transactionProvider: TransactionProvider,
    private val calculateCartUseCase: CalculateCartUseCase = CalculateCartUseCase()
) {
    suspend operator fun invoke(
        items: List<CartItem>, 
        strategy: DiscountStrategy,
        paymentMethod: String,
        paymentReference: String?,
        cashierId: String? = null,
        tableLabel: String? = null
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
            cashierId = cashierId,
            tableLabel = tableLabel,
            items = orderItems
        )
        return try {
            transactionProvider.runAsTransaction {
                orderRepository.saveOrder(order)
                
                items.forEach { cartItem ->
                    val components = ComboBundleResolver.expand(
                        cartItem.item.id,
                        cartItem.variant.id,
                        cartItem.quantity
                    )
                    components.forEach { component ->
                        val mappings = recipeRepository.getMappingsForCheckout(
                            component.menuItemId,
                            component.variantName
                        )
                        mappings.forEach { mapping ->
                            val totalDeduction = mapping.deductionQuantity * component.quantity
                            if (totalDeduction > 0) {
                                inventoryRepository.decrementStock(mapping.inventoryItemId, totalDeduction)
                            }
                        }
                    }
                }
            }
            
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
