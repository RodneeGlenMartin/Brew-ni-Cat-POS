package com.example.cattasticpos

import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.CartKey
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.model.Variant
import com.example.cattasticpos.domain.usecase.ComboBundleResolver
import com.example.cattasticpos.worker.SyncWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * The cloud order-id contract and combo expansion. OrderSyncMerger recovers a local id with
 * `remoteId % 1_000_000_000`, so these two must never drift apart.
 */
class SyncIdentityTest {

    private val partition = 1_000_000_000L

    @Test
    fun `remote ids are always positive`() {
        repeat(500) {
            val id = SyncWorker.getSupabaseOrderId(UUID.randomUUID().toString(), 1L)
            assertTrue("negative id $id", id > 0)
        }
    }

    @Test
    fun `the local id is recoverable from the remote id`() {
        val deviceId = "b3f1c0de-0000-4000-8000-000000000001"
        listOf(1L, 42L, 9_999L, 999_999_999L).forEach { localId ->
            val remote = SyncWorker.getSupabaseOrderId(deviceId, localId)
            assertEquals(localId, remote % partition)
        }
    }

    @Test
    fun `the same device always maps to the same bucket`() {
        val deviceId = UUID.randomUUID().toString()
        val first = SyncWorker.getSupabaseOrderId(deviceId, 7L)
        val second = SyncWorker.getSupabaseOrderId(deviceId, 7L)
        assertEquals(first, second)
    }

    @Test
    fun `different devices do not collide on the same local id`() {
        val a = SyncWorker.getSupabaseOrderId("device-a", 5L)
        val b = SyncWorker.getSupabaseOrderId("device-b", 5L)
        assertNotEquals(a, b)
    }

    @Test
    fun `order item ids stay inside their order's slot without overflowing`() {
        // Guards the bucket width: getSupabaseOrderItemId multiplies by another 1000, so any
        // widening of DEVICE_BUCKETS silently wraps Long and corrupts every item id.
        repeat(200) {
            val orderId = SyncWorker.getSupabaseOrderId(UUID.randomUUID().toString(), 999L)
            val itemId = SyncWorker.getSupabaseOrderItemId(orderId, 3)
            assertTrue("item id overflowed: $itemId", itemId > 0)
            assertEquals(orderId, itemId / 1000L)
        }
    }

    private fun comboCartItem(variantId: String, quantity: Int): CartItem {
        val variant = Variant(id = variantId, name = "Combo", basePrice = 105.0)
        val item = Item(
            id = "combo_single_paw",
            categoryId = "cat_combos",
            name = "Single Paw Combo",
            flavors = emptyList(),
            variants = listOf(variant)
        )
        return CartItem(
            key = CartKey.from(item, variant, null),
            item = item,
            variant = variant,
            flavor = null,
            quantity = quantity
        )
    }

    @Test
    fun `combo expansion scales component quantities by the ordered quantity`() {
        // combo_1 = 1 x takoyaki 4pcs + 1 x 16oz coffee.
        val components = ComboBundleResolver.expandFromCartItem(comboCartItem("combo_1", quantity = 3))
        assertEquals(2, components.size)
        assertTrue(components.all { it.quantity == 3 })
        assertEquals(
            setOf("bite_takoyaki", "drink_cat_feine"),
            components.map { it.menuItemId }.toSet()
        )
    }

    @Test
    fun `components with a multiplier greater than one compound with the ordered quantity`() {
        // combo_5 = 1 x takoyaki 8pcs + 2 x 16oz soda; ordering 2 means 4 sodas.
        val components = ComboBundleResolver.expandFromCartItem(comboCartItem("combo_5", quantity = 2))
        val soda = components.single { it.menuItemId == "drink_soda" }
        assertEquals(4, soda.quantity)
        assertEquals(2, components.single { it.menuItemId == "bite_takoyaki" }.quantity)
    }

    @Test
    fun `a non-combo item passes straight through`() {
        val components = ComboBundleResolver.expand(
            menuItemId = "bite_fries",
            variantId = "small",
            sizeVariantName = "Small",
            flavor = "Cheese",
            orderQuantity = 4
        )
        assertEquals(1, components.size)
        assertEquals("bite_fries", components[0].menuItemId)
        assertEquals("Small", components[0].sizeVariantName)
        assertEquals("Cheese", components[0].flavor)
        assertEquals(4, components[0].quantity)
    }

    @Test
    fun `an unknown combo variant expands to nothing rather than guessing`() {
        val components = ComboBundleResolver.expandFromCartItem(comboCartItem("combo_does_not_exist", 1))
        assertTrue(components.isEmpty())
    }
}
