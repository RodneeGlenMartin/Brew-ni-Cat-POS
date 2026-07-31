package com.example.cattasticpos

import com.example.cattasticpos.data.local.MenuBoardCatalog
import com.example.cattasticpos.domain.catalog.ProductAddOnCatalog
import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.CartKey
import com.example.cattasticpos.domain.model.CartLineSelection
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.model.Variant
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The add-on surcharge has to survive the trip from the configuration sheet into the cart.
 *
 * Every add-on test written before this one hand-fed the stored string with a leading " + ",
 * which is the one shape `encode()` never produces for an item that has no flavor — so the sheet
 * could show "Price Summary ₱134" while the cart charged ₱119 and the whole suite stayed green.
 * These drive the real path instead: sheet selections -> encode() -> parse() -> CartItem.unitPrice.
 */
class AddOnEncodingTest {

    private val menu: List<Item> = MenuBoardCatalog.allMenuItems().map { entity ->
        val variants = JSONArray(entity.variantsJson)
        Item(
            id = entity.id,
            categoryId = entity.categoryId,
            name = entity.name,
            flavors = entity.flavors.split("|").filter { it.isNotBlank() },
            variants = (0 until variants.length()).map { index ->
                val json = variants.getJSONObject(index)
                val byFlavor = json.optJSONObject("priceByFlavor")
                Variant(
                    id = json.getString("id"),
                    name = json.getString("name"),
                    basePrice = json.getDouble("basePrice"),
                    priceByFlavor = byFlavor?.let { prices ->
                        prices.keys().asSequence().associateWith { prices.getDouble(it) }
                    }.orEmpty()
                )
            }
        )
    }

    /** Exactly what the sheet's footer prints as "Price Summary". */
    private fun sheetPrice(item: Item, variant: Variant, flavor: String?, addOnIds: List<String>): Double {
        val base = if (flavor == null && variant.basePrice == 0.0) 0.0 else variant.getPrice(flavor)
        return base + ProductAddOnCatalog.surcharge(item, addOnIds)
    }

    /** Exactly what "Add to Order" puts in the cart. */
    private fun cartPrice(item: Item, variant: Variant, flavor: String?, addOnIds: List<String>): Double {
        val stored = CartLineSelection.encode(flavor, null, addOnIds, item.id)
        return CartItem(
            key = CartKey.from(item, variant, stored),
            item = item,
            variant = variant,
            flavor = stored,
            quantity = 1
        ).unitPrice
    }

    @Test
    fun `an egg on a Buldak reaches the cart at the price the sheet quoted`() {
        val item = menu.first { it.id == "buldak_carbo" }
        val plain = item.variants.first { it.id == "plain_carbo" }

        // Plain Carbo Samyang 119 + Egg (Sunny Side Up) 15.
        assertEquals(134.0, sheetPrice(item, plain, null, listOf("egg_sunny")), 0.001)
        assertEquals(134.0, cartPrice(item, plain, null, listOf("egg_sunny")), 0.001)
    }

    @Test
    fun `the sheet price and the cart price agree for every add-on the menu offers`() {
        val mismatches = mutableListOf<String>()
        for (item in menu) {
            val offered = ProductAddOnCatalog.addOnsForItem(item)
            if (offered.isEmpty()) continue
            // The sheet refuses "Add to Order" until a flavor is picked, so mirror that here.
            val flavor = item.flavors.firstOrNull()
            val selections = offered.map { listOf(it.id) } + listOf(offered.map { it.id })
            for (variant in item.variants) {
                for (ids in selections) {
                    val quoted = sheetPrice(item, variant, flavor, ids)
                    val charged = cartPrice(item, variant, flavor, ids)
                    if (kotlin.math.abs(quoted - charged) > 0.001) {
                        mismatches += "${item.id}/${variant.id} $ids: sheet quoted $quoted, cart charged $charged"
                    }
                }
            }
        }
        assertTrue(mismatches.joinToString("\n"), mismatches.isEmpty())
    }

    @Test
    fun `add-ons survive a round trip even with no flavor to hang them off`() {
        val stored = CartLineSelection.encode(null, null, listOf("egg_boiled", "hotdog"), "sedaap_original")
        val parsed = CartLineSelection.parse(stored, "sedaap_original")
        assertEquals(null, parsed.baseFlavor)
        assertEquals(listOf("Egg (Boiled)", "Hotdog"), parsed.addOnLabels)
        assertEquals(35.0, parsed.addOnSurcharge("sedaap_original"), 0.001)
    }

    @Test
    fun `a bare label left behind by an older build is still read as an add-on`() {
        // Builds 10122-10125 stored "Egg (Sunny Side Up)" with no separator, so it parsed back
        // as the flavor and its 15 vanished. Those rows must keep working after the fix.
        val parsed = CartLineSelection.parse("Egg (Sunny Side Up)", "buldak_carbo")
        assertEquals(listOf("Egg (Sunny Side Up)"), parsed.addOnLabels)
        assertEquals(null, parsed.baseFlavor)
        assertEquals(15.0, parsed.addOnSurcharge("buldak_carbo"), 0.001)

        val multiple = CartLineSelection.parse("Hotdog, Fries", "buldak_cheese")
        assertEquals(listOf("Hotdog", "Fries"), multiple.addOnLabels)
        assertEquals(50.0, multiple.addOnSurcharge("buldak_cheese"), 0.001)
    }

    @Test
    fun `a real flavor is never mistaken for an add-on`() {
        // The repair above only holds while no item has a flavor named like one of its own
        // add-ons. If anyone ever adds a "Fries" flavor to a Buldak, this fails loudly.
        val collisions = mutableListOf<String>()
        for (item in menu) {
            val labels = ProductAddOnCatalog.knownAddOnsForItem(item).map { it.label }.toSet()
            item.flavors.forEach { flavor ->
                if (flavor.trim() in labels) collisions += "${item.id}: flavor '$flavor' is also an add-on"
            }
        }
        assertTrue(collisions.joinToString("\n"), collisions.isEmpty())

        // A genuine flavor with no add-ons attached stays a flavor.
        val parsed = CartLineSelection.parse("Yogurt Yarn", "drink_soda")
        assertEquals("Yogurt Yarn", parsed.baseFlavor)
        assertTrue(parsed.addOnLabels.isEmpty())
    }

    @Test
    fun `a flavor and add-ons together still separate cleanly`() {
        val stored = CartLineSelection.encode("Yogurt Yarn", null, listOf("nata"), "drink_soda")
        assertEquals("Yogurt Yarn + Nata de coco", stored)
        val parsed = CartLineSelection.parse(stored, "drink_soda")
        assertEquals("Yogurt Yarn", parsed.baseFlavor)
        assertEquals(listOf("Nata de coco"), parsed.addOnLabels)
    }

    @Test
    fun `an add-on with no flavor deducts its own inventory`() {
        // Nata de coco has a recipe row; the resolver finds add-ons by the same separator that
        // encode() writes, so a missing separator would silently stop deducting stock too.
        val mappings = listOf(
            com.example.cattasticpos.domain.model.RecipeMapping("r_soda_all", "drink_soda", null, "inv_cups", 1.0),
            com.example.cattasticpos.domain.model.RecipeMapping("r_soda_nata", "drink_soda", "Nata de coco", "inv_nata_coco", 1.0)
        )
        val resolved = com.example.cattasticpos.domain.usecase.RecipeDeductionResolver.resolve(
            mappings, "12oz", "+ Nata de coco"
        )
        assertEquals(listOf("inv_cups", "inv_nata_coco"), resolved.map { it.inventoryItemId })
    }
}
