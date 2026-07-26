package com.example.cattasticpos

import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.CartKey
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.model.Order
import com.example.cattasticpos.domain.model.OrderItem
import com.example.cattasticpos.domain.model.Variant
import com.example.cattasticpos.domain.strategy.FivePercentDiscountStrategy
import com.example.cattasticpos.domain.strategy.FreeOrderDiscountStrategy
import com.example.cattasticpos.domain.strategy.NoDiscountStrategy
import com.example.cattasticpos.domain.strategy.PercentageDiscountStrategy
import com.example.cattasticpos.domain.usecase.CalculateCartUseCase
import com.example.cattasticpos.domain.usecase.OrderCartMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the cashier reads off the screen has to be the number the database keeps, or the drawer
 * never reconciles. Everything is formatted with "%.0f", so every stored figure must be whole.
 */
class DiscountRoundingTest {

    private val calculate = CalculateCartUseCase()

    private fun cart(unitPrice: Double, quantity: Int = 1): List<CartItem> {
        val variant = Variant(id = "v", name = "Regular", basePrice = unitPrice)
        val item = Item("bite_fries", "cat_bites", "Fries", emptyList(), listOf(variant))
        return listOf(CartItem(CartKey.from(item, variant, null), item, variant, null, quantity))
    }

    private fun whole(value: Double) = value == Math.floor(value)

    @Test
    fun `a half-peso discount does not leave centavos in the total`() {
        // 5% of 90 is exactly 4.50 — the case that printed "90 - 5 = 86".
        val result = calculate(cart(90.0), FivePercentDiscountStrategy())
        assertEquals(90.0, result.subtotal, 0.001)
        assertEquals(5.0, result.discountDeduction, 0.001)
        assertEquals(85.0, result.total, 0.001)
    }

    @Test
    fun `the printed receipt always adds up`() {
        val strategies = listOf(
            NoDiscountStrategy(),
            FivePercentDiscountStrategy(),
            PercentageDiscountStrategy(10.0),
            PercentageDiscountStrategy(20.0),
            FreeOrderDiscountStrategy()
        )
        for (subtotal in 10..500 step 10) {
            for (strategy in strategies) {
                val r = calculate(cart(subtotal.toDouble()), strategy)
                assertTrue("deduction not whole: ${r.discountDeduction}", whole(r.discountDeduction))
                assertTrue("total not whole: ${r.total}", whole(r.total))
                assertEquals(
                    "subtotal - discount != total for $subtotal / ${r.discountLabel}",
                    r.subtotal - r.discountDeduction,
                    r.total,
                    0.001
                )
            }
        }
    }

    @Test
    fun `a discount can never exceed the subtotal or push the total negative`() {
        val r = calculate(cart(40.0), PercentageDiscountStrategy(250.0))
        assertEquals(40.0, r.discountDeduction, 0.001)
        assertEquals(0.0, r.total, 0.001)
    }

    @Test
    fun `a free order is still free`() {
        val r = calculate(cart(99.0, quantity = 3), FreeOrderDiscountStrategy())
        assertEquals(297.0, r.subtotal, 0.001)
        assertEquals(0.0, r.total, 0.001)
    }

    // --- discount label round-trip (receipt editor) ---

    private fun labelFor(strategy: com.example.cattasticpos.domain.strategy.DiscountStrategy) =
        strategy.applyDiscount(100.0, emptyList()).label

    @Test
    fun `a free order reopens as a free order, not as ten percent off`() {
        val label = labelFor(FreeOrderDiscountStrategy())
        assertEquals("100% Free Order Coupon Applied", label)
        val restored = OrderCartMapper.discountStrategyFromLabel(label)
        assertTrue("restored as ${restored::class.simpleName}", restored is FreeOrderDiscountStrategy)
        // The bill must still come out at zero after a round-trip through the editor.
        assertEquals(0.0, calculate(cart(250.0), restored).total, 0.001)
    }

    @Test
    fun `every discount label round-trips to an equivalent strategy`() {
        val strategies = listOf(
            NoDiscountStrategy(),
            FivePercentDiscountStrategy(),
            PercentageDiscountStrategy(10.0),
            PercentageDiscountStrategy(20.0),
            FreeOrderDiscountStrategy()
        )
        for (original in strategies) {
            val restored = OrderCartMapper.discountStrategyFromLabel(labelFor(original))
            assertEquals(
                "label '${labelFor(original)}' restored to the wrong deduction",
                calculate(cart(240.0), original).total,
                calculate(cart(240.0), restored).total,
                0.001
            )
        }
    }

    @Test
    fun `an unrecognised label falls back to no discount`() {
        val restored = OrderCartMapper.discountStrategyFromLabel("Staff Meal")
        assertTrue(restored is NoDiscountStrategy)
        assertEquals(240.0, calculate(cart(240.0), restored).total, 0.001)
    }

    // --- reopening an order whose variant left the menu ---

    @Test
    fun `reopening an order does not charge its add-ons twice`() {
        val order = Order(
            id = 1L,
            timestamp = 0L,
            subtotal = 135.0,
            discountDeduction = 0.0,
            discountLabel = "None",
            total = 135.0,
            paymentMethod = "CASH",
            paymentReference = null,
            cashierId = null,
            cashierName = null,
            tableLabel = null,
            isServed = false,
            items = listOf(
                OrderItem(
                    id = 1L,
                    orderId = 1L,
                    itemId = "buldak_carbo",
                    itemName = "Buldak Carbonara",
                    variantId = "variant_that_no_longer_exists",
                    variantName = "Regular",
                    flavor = " + Egg (Sunny Side Up)", // 120 base + 15 add-on
                    quantity = 1,
                    unitPrice = 135.0,
                    totalPrice = 135.0
                )
            )
        )
        // Menu no longer carries that variant, so the mapper has to rebuild it from unitPrice.
        val restored = OrderCartMapper.orderToCartItems(order, menu = emptyList())
        assertEquals(1, restored.size)
        assertEquals(135.0, restored[0].unitPrice, 0.001)
        assertEquals(135.0, calculate(restored, NoDiscountStrategy()).total, 0.001)
    }
}
