package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.CartKey
import com.example.cattasticpos.domain.model.CartLineSelection
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

    private val PERCENT_LABEL = Regex("""^(\d+(?:\.\d+)?)\s*%""")

    /**
     * Rebuilds the strategy behind a stored [Order.discountLabel] so the receipt editor reopens
     * an order with the discount it was rung up under.
     *
     * The free-order label is "100% Free Order Coupon Applied", which `startsWith("10")` matched
     * before the free branch was ever reached — reopening a free order in the editor turned it
     * into a 10% discount and saving it charged the customer 90% of the bill. Free is checked
     * first now, and the percentage is parsed rather than prefix-matched.
     */
    fun discountStrategyFromLabel(label: String): DiscountStrategy {
        val normalized = label.trim()
        if (normalized.contains("Free", ignoreCase = true)) return FreeOrderDiscountStrategy()
        if (normalized.isBlank() || normalized.equals("None", ignoreCase = true)) return NoDiscountStrategy()
        val pct = PERCENT_LABEL.find(normalized)?.groupValues?.get(1)?.toDoubleOrNull()
            ?: return NoDiscountStrategy()
        return if (pct == 5.0) FivePercentDiscountStrategy() else PercentageDiscountStrategy(pct)
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
        // The stored unitPrice already includes any add-on surcharge, but CartItem.unitPrice adds
        // the surcharge back on top of the variant's base price. Using unitPrice as the base for
        // a variant the menu no longer has (renamed/removed by a catalog update) therefore
        // charged every add-on twice as soon as the order was reopened in the editor.
        val addOnSurcharge = CartLineSelection.parse(flavor, itemId).addOnSurcharge(itemId)
        val variant = menuItem?.variants?.find { it.id == variantId }
            ?: Variant(
                id = variantId,
                name = variantName,
                basePrice = (unitPrice - addOnSurcharge).coerceAtLeast(0.0),
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
