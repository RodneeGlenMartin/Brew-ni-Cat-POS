package com.example.cattasticpos

import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.CartKey
import com.example.cattasticpos.domain.model.CartLineSelection
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.model.Variant
import com.example.cattasticpos.domain.strategy.FivePercentDiscountStrategy
import com.example.cattasticpos.domain.strategy.FreeOrderDiscountStrategy
import com.example.cattasticpos.domain.strategy.NoDiscountStrategy
import com.example.cattasticpos.domain.strategy.PercentageDiscountStrategy
import com.example.cattasticpos.domain.usecase.CalculateCartUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Cart money maths — every figure on the receipt and the Z-Reading starts here. */
class CartPricingTest {

    private val calculate = CalculateCartUseCase()

    private fun buldak(quantity: Int, flavor: String?): CartItem {
        val variant = Variant(id = "regular", name = "Regular", basePrice = 120.0)
        val item = Item(
            id = "buldak_carbo",
            categoryId = "cat_buldak",
            name = "Buldak Carbonara",
            flavors = emptyList(),
            variants = listOf(variant)
        )
        return CartItem(
            key = CartKey.from(item, variant, flavor),
            item = item,
            variant = variant,
            flavor = flavor,
            quantity = quantity
        )
    }

    @Test
    fun `line total multiplies unit price by quantity`() {
        assertEquals(360.0, buldak(quantity = 3, flavor = null).totalPrice, 0.001)
    }

    @Test
    fun `add-on surcharges apply per unit, not once per line`() {
        assertEquals(240.0, buldak(quantity = 2, flavor = null).totalPrice, 0.001)

        // Sunny-side-up egg is +15, so 2 x (120 + 15) = 270 — not (120 x 2) + 15.
        val withEgg = buldak(quantity = 2, flavor = " + Egg (Sunny Side Up)")
        assertEquals(135.0, withEgg.unitPrice, 0.001)
        assertEquals(270.0, withEgg.totalPrice, 0.001)
    }

    @Test
    fun `unknown add-on labels contribute nothing`() {
        val bogus = buldak(quantity = 1, flavor = " + Truffle Caviar")
        assertEquals(120.0, bogus.unitPrice, 0.001)
    }

    @Test
    fun `no discount leaves the subtotal untouched`() {
        val result = calculate(listOf(buldak(2, null)), NoDiscountStrategy())
        assertEquals(240.0, result.subtotal, 0.001)
        assertEquals(0.0, result.discountDeduction, 0.001)
        assertEquals(240.0, result.total, 0.001)
    }

    @Test
    fun `percentage discount deducts from the subtotal`() {
        val result = calculate(listOf(buldak(1, null)), PercentageDiscountStrategy(20.0))
        assertEquals(24.0, result.discountDeduction, 0.001)
        assertEquals(96.0, result.total, 0.001)
        assertEquals("20% OFF", result.discountLabel)
    }

    @Test
    fun `five percent discount matches the generic percentage strategy`() {
        val items = listOf(buldak(3, null))
        assertEquals(
            calculate(items, PercentageDiscountStrategy(5.0)).total,
            calculate(items, FivePercentDiscountStrategy()).total,
            0.001
        )
    }

    @Test
    fun `free order coupon zeroes the total without going negative`() {
        val result = calculate(listOf(buldak(4, null)), FreeOrderDiscountStrategy())
        assertEquals(480.0, result.subtotal, 0.001)
        assertEquals(0.0, result.total, 0.001)
    }

    @Test
    fun `empty cart totals zero`() {
        val result = calculate(emptyList(), NoDiscountStrategy())
        assertEquals(0.0, result.subtotal, 0.001)
        assertEquals(0.0, result.total, 0.001)
    }

    @Test
    fun `total never drops below zero even if a discount overshoots`() {
        val result = calculate(listOf(buldak(1, null)), PercentageDiscountStrategy(150.0))
        assertTrue("total was ${result.total}", result.total >= 0.0)
    }

    @Test
    fun `flavor strings round-trip through parse and encode`() {
        val encoded = CartLineSelection.encode(
            baseFlavor = "Cheese",
            coffeeOption = "Hot",
            addOnIds = listOf("takeout_box"),
            itemId = "buldak_carbo"
        )
        val parsed = CartLineSelection.parse(encoded, "buldak_carbo")
        assertEquals("Cheese", parsed.baseFlavor)
        assertEquals("Hot", parsed.coffeeOption)
        assertEquals(listOf("Take-out Box"), parsed.addOnLabels)
        assertEquals(10.0, parsed.addOnSurcharge("buldak_carbo"), 0.001)
    }

    @Test
    fun `a blank flavor parses to an empty selection`() {
        val parsed = CartLineSelection.parse(null, "bite_fries")
        assertEquals(null, parsed.baseFlavor)
        assertEquals(null, parsed.coffeeOption)
        assertTrue(parsed.addOnLabels.isEmpty())
    }
}
