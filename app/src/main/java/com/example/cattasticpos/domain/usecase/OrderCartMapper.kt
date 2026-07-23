package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.CartKey
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.model.Order
import com.example.cattasticpos.domain.model.OrderItem
import com.example.cattasticpos.domain.model.Variant
import com.example.cattasticpos.domain.strategy.DiscountStrategy
import com.example.cattasticpos.domain.strategy.FivePercentDiscountStrategy
import com.example.cattasticpos.domain.strategy.FreeOrderDiscountStrategy
import com.example.cattasticpos.domain.strategy.NoDiscountStrategy
import com.example.cattasticpos.domain.strategy.PercentageDiscountStrategy

object OrderCartMapper {
    fun orderToCartItems(order: Order, menu: List<Item>): List<CartItem> {
        return order.items.mapNotNull { orderItem ->
            orderItem.toCartItem(menu)
        }
    }

    fun cartItemsToOrderItems(orderId: Long, cartItems: List<CartItem>): List<OrderItem> {
        return cartItems.map { cartItem ->
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
    }

    fun discountStrategyFromLabel(label: String): DiscountStrategy {
        return when {
            label.equals("None", ignoreCase = true) -> NoDiscountStrategy()
            label.equals("5% OFF", ignoreCase = true) -> FivePercentDiscountStrategy()
            label.startsWith("10") -> PercentageDiscountStrategy(10.0)
            label.startsWith("20") -> PercentageDiscountStrategy(20.0)
            label.contains("Free", ignoreCase = true) -> FreeOrderDiscountStrategy()
            else -> NoDiscountStrategy()
        }
    }

    fun previewOrder(
        original: Order,
        cartItems: List<CartItem>,
        calculation: CartCalculationResult
    ): Order {
        return original.copy(
            subtotal = calculation.subtotal,
            discountDeduction = calculation.discountDeduction,
            discountLabel = calculation.discountLabel,
            total = calculation.total,
            items = cartItemsToOrderItems(original.id, cartItems)
        )
    }

    private fun OrderItem.toCartItem(menu: List<Item>): CartItem? {
        val menuItem = menu.find { it.id == itemId }
        val variant = menuItem?.variants?.find { it.id == variantId }
            ?: Variant(
                id = variantId,
                name = variantName,
                basePrice = unitPrice,
                priceByFlavor = emptyMap()
            )
        val item = menuItem ?: Item(
            id = itemId,
            categoryId = "",
            name = itemName,
            flavors = flavor?.let { listOf(it) }.orEmpty(),
            variants = listOf(variant)
        )
        return CartItem(
            key = CartKey(itemId = itemId, variantId = variant.id, flavor = flavor),
            item = item,
            variant = variant,
            flavor = flavor,
            quantity = quantity
        )
    }
}
