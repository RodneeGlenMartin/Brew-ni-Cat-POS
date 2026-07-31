package com.example.cattasticpos

import com.example.cattasticpos.data.local.AddOnSurchargeRepair
import com.example.cattasticpos.domain.catalog.ProductAddOnCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The 18 -> 19 migration rewrites money in orders that were already rung up, so its decisions are
 * pinned here rather than trusted. The five real rows below are the ones the live database
 * actually had, surveyed before the repair was written.
 */
class AddOnSurchargeRepairTest {

    @Test
    fun `it repairs exactly the rows the live database had`() {
        // itemId, stored flavor, stored unit price -> corrected flavor, corrected unit price
        val cases = listOf(
            Triple("sedaap_original", "Fries", 65.0) to ("+ Fries" to 95.0),
            Triple("buldak_carbo", "Egg (Omelette)", 135.0) to ("+ Egg (Omelette)" to 150.0),
            Triple("buldak_carbo", "Egg (Sunny Side Up)", 129.0) to ("+ Egg (Sunny Side Up)" to 144.0),
            Triple("buldak_carbo", "Egg (Sunny Side Up)", 135.0) to ("+ Egg (Sunny Side Up)" to 150.0),
            Triple("buldak_cheese", "Egg (Omelette)", 109.0) to ("+ Egg (Omelette)" to 124.0)
        )
        cases.forEach { (input, expected) ->
            val (itemId, flavor, unitPrice) = input
            val fix = AddOnSurchargeRepair.fixFor(itemId, flavor, unitPrice)
            assertEquals("$itemId / $flavor", expected, fix)
        }
    }

    @Test
    fun `it leaves every other flavor alone`() {
        // Real flavors from the same five orders — none of these may be touched.
        val untouched = listOf(
            "drink_cat_feine" to "Salted Caramel Tail — With Coffee",
            "drink_matcha" to "Vanilla Matcha Meow — Without Coffee",
            "drink_matcha" to "Salted Caramel Meow-cha — Without Coffee",
            "bite_takoyaki" to "Veggie Whiskers",
            "bite_takoyaki" to "Squid Treats",
            "bite_fries" to "Cheesy Purr",
            "drink_soda" to "Yogurt Yarn",
            // "Fries" is a Buldak add-on, but on the fries SKU it is the item itself.
            "bite_fries" to "Fries",
            "bite_takeout_box" to null,
            "buldak_carbo" to null,
            "buldak_carbo" to "",
            "sedaap_spicy_chicken" to "2 Paws (Medium)"
        )
        untouched.forEach { (itemId, flavor) ->
            assertNull("$itemId / $flavor", AddOnSurchargeRepair.fixFor(itemId, flavor, 100.0))
        }
    }

    @Test
    fun `a repaired row is never repaired again`() {
        val once = AddOnSurchargeRepair.fixFor("buldak_carbo", "Egg (Sunny Side Up)", 119.0)
        assertEquals("+ Egg (Sunny Side Up)" to 134.0, once)
        // Re-running the migration, or syncing a row a repaired device already pushed, must be a
        // no-op — otherwise the customer is charged for the egg twice.
        assertNull(AddOnSurchargeRepair.fixFor("buldak_carbo", once!!.first, once.second))
        // The shape the fixed app writes for a flavor plus an extra is likewise already correct.
        assertNull(AddOnSurchargeRepair.fixFor("drink_soda", "Yogurt Yarn + Nata de coco", 49.0))
    }

    @Test
    fun `several extras on one line all come back`() {
        assertEquals(
            "+ Hotdog, Fries" to 159.0,
            AddOnSurchargeRepair.fixFor("buldak_cheese", "Hotdog, Fries", 109.0)
        )
    }

    @Test
    fun `the frozen price table still agrees with the menu`() {
        // The migration keeps its own copy so a future price change cannot rewrite history. That
        // is only safe while the two match today; this fails the moment they diverge, so whoever
        // changes a price has to decide consciously what happens to the repair.
        val catalogItems = listOf(
            "buldak_carbo", "buldak_cheese", "sedaap_original", "sedaap_spicy_chicken",
            "drink_soda", "bite_takoyaki", "bite_fries", "bite_nachos"
        )
        val drift = mutableListOf<String>()
        catalogItems.forEach { itemId ->
            ProductAddOnCatalog.knownAddOnsForItem(itemId).forEach { option ->
                val repaired = AddOnSurchargeRepair.fixFor(itemId, option.label, 0.0)
                if (repaired == null) {
                    drift += "$itemId: the repair does not recognise '${option.label}'"
                } else if (kotlin.math.abs(repaired.second - option.price) > 0.001) {
                    drift += "$itemId/${option.label}: catalog charges ${option.price}, " +
                        "the repair adds back ${repaired.second}"
                }
            }
        }
        assertTrue(drift.joinToString("\n"), drift.isEmpty())
    }

    @Test
    fun `order totals are re-derived from the label the order was rung up under`() {
        assertEquals(0.0, AddOnSurchargeRepair.deductionFor("None", 418.0), 0.001)
        assertEquals(0.0, AddOnSurchargeRepair.deductionFor("", 418.0), 0.001)
        assertEquals(0.0, AddOnSurchargeRepair.deductionFor(null, 418.0), 0.001)
        // 5% of 418 is 20.90 and 20% is 83.60; both settle on a whole peso, exactly as the cart
        // does, so the receipt and the stored figure stay the same number.
        assertEquals(21.0, AddOnSurchargeRepair.deductionFor("5% OFF", 418.0), 0.001)
        assertEquals(84.0, AddOnSurchargeRepair.deductionFor("20% OFF", 418.0), 0.001)
        // Free must be recognised before the percentage, or "100% Free Order Coupon Applied"
        // parses as a 100% discount by accident and only happens to land on the same number.
        assertEquals(418.0, AddOnSurchargeRepair.deductionFor("100% Free Order Coupon Applied", 418.0), 0.001)
        // Rounded HALF_UP like every other money figure, so 4.50 becomes 5 and not 4.
        assertEquals(5.0, AddOnSurchargeRepair.deductionFor("5% OFF", 90.0), 0.001)
        assertTrue(AddOnSurchargeRepair.deductionFor("50% OFF", 0.0) == 0.0)
    }

    @Test
    fun `the repair agrees with what the app would charge for the same line today`() {
        // End to end: an old row, repaired, must price exactly as the fixed cart prices it.
        val repaired = AddOnSurchargeRepair.fixFor("buldak_cheese", "Egg (Omelette)", 109.0)!!
        val parsed = com.example.cattasticpos.domain.model.CartLineSelection.parse(
            repaired.first, "buldak_cheese"
        )
        assertEquals(listOf("Egg (Omelette)"), parsed.addOnLabels)
        assertEquals(repaired.second, 109.0 + parsed.addOnSurcharge("buldak_cheese"), 0.001)
    }
}
