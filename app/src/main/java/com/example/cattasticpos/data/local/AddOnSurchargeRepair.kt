package com.example.cattasticpos.data.local

import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * One-time repair of add-on surcharges that were never charged.
 *
 * `CartLineSelection.encode()` only wrote the " + " separator when something already sat in front
 * of it. Buldak Carbo, Buldak Cheese and Sedaap Original ship with no flavour list, so choosing
 * an extra on one of them stored a bare "Egg (Sunny Side Up)" — which parsed straight back as the
 * flavour, leaving the surcharge out of `unitPrice`. The configuration sheet quoted PHP 134 and
 * the cart charged PHP 119.
 *
 * Rows are recognised by that signature: a flavour made up purely of the item's own add-on labels
 * with no separator anywhere. The fix adds the missing surcharge, rewrites the flavour into the
 * separator form the app now reads, re-derives the parent order's totals, and marks the order
 * PENDING so [com.example.cattasticpos.worker.SyncWorker] republishes it to the cloud (which the
 * download path would otherwise never correct — it only ever syncs status fields back down).
 *
 * Idempotent: an already-repaired row carries the separator and is skipped, so running this twice
 * cannot charge an extra twice.
 */
internal object AddOnSurchargeRepair {

    private const val TAG = "AddOnSurchargeRepair"

    /**
     * Deliberately frozen rather than read from [com.example.cattasticpos.domain.catalog.ProductAddOnCatalog]:
     * a migration has to keep producing the same pesos for ever, and a future price change on the
     * menu must not retroactively rewrite what a customer was charged in July 2026.
     * `AddOnSurchargeRepairTest` asserts this table still agrees with the catalog, so the
     * duplication is checked rather than assumed.
     */
    private val prices = mapOf(
        "Egg (Sunny Side Up)" to 15.0,
        "Egg (Omelette)" to 15.0,
        "Egg (Boiled)" to 15.0,
        "3pcs Seaweed" to 25.0,
        "Hotdog" to 20.0,
        "Fries" to 30.0,
        "Nata de coco" to 10.0,
        "Rainbow Jelly" to 10.0,
        "Take-out Box" to 10.0
    )

    private val buldakLabels = setOf(
        "Egg (Sunny Side Up)", "Egg (Omelette)", "Egg (Boiled)",
        "3pcs Seaweed", "Hotdog", "Fries", "Take-out Box"
    )
    private val sodaLabels = setOf("Nata de coco", "Rainbow Jelly", "Take-out Box")
    private val retiredOnly = setOf("Take-out Box")

    /**
     * Labels this item could legitimately have carried, mirroring how the add-on catalog routed
     * an item id while the bug was live. Scoping by item is what stops a real flavour from being
     * mistaken for an extra — "Fries" is a Buldak add-on, but on `bite_fries` it is a menu item
     * whose own flavours ("Cheesy Purr") are not in this set.
     */
    fun labelsFor(itemId: String): Set<String> {
        val id = itemId.lowercase()
        return when {
            id == "bite_takeout_box" -> retiredOnly
            id == "drink_soda" || id.contains("soda") -> sodaLabels
            id.startsWith("buldak") || id.startsWith("sedaap") ||
                id.contains("buldak") || id.contains("sedaap") -> buldakLabels
            else -> retiredOnly
        }
    }

    /**
     * The corrected flavour string and unit price for a stored line, or null to leave it alone.
     */
    fun fixFor(itemId: String, flavor: String?, unitPrice: Double): Pair<String, Double>? {
        val text = flavor?.trim().orEmpty()
        if (text.isEmpty()) return null
        // Any separator means the line was stored by a build that got this right.
        if (text.contains(" + ") || text.startsWith("+ ") || text.contains(" — ")) return null

        val labels = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val allowed = labelsFor(itemId)
        if (labels.isEmpty() || labels.any { it !in allowed }) return null

        val surcharge = labels.sumOf { prices.getValue(it) }
        return "+ ${labels.joinToString(", ")}" to unitPrice + surcharge
    }

    /**
     * Re-derives a discount from the label the order was rung up under. Mirrors
     * `OrderCartMapper.discountStrategyFromLabel` plus `CalculateCartUseCase`'s HALF_UP rounding
     * so a repaired order agrees with one rung up today.
     */
    fun deductionFor(discountLabel: String?, subtotal: Double): Double {
        val label = discountLabel?.trim().orEmpty()
        if (label.contains("Free", ignoreCase = true)) return subtotal
        val pct = PERCENT_LABEL.find(label)?.groupValues?.get(1)?.toDoubleOrNull() ?: return 0.0
        return Math.round(subtotal * pct / 100.0).toDouble().coerceIn(0.0, subtotal)
    }

    private val PERCENT_LABEL = Regex("""^(\d+(?:\.\d+)?)\s*%""")

    private data class PendingFix(
        val rowId: Long,
        val orderId: Long,
        val flavor: String,
        val unitPrice: Double,
        val totalPrice: Double
    )

    /** @return the number of line items repaired. */
    fun run(db: SupportSQLiteDatabase): Int {
        val fixes = mutableListOf<PendingFix>()
        // Collected first, applied after: updating a table while its own cursor is still open is
        // asking for trouble.
        db.query(
            "SELECT id, orderId, itemId, flavor, quantity, unitPrice FROM order_items " +
                "WHERE flavor IS NOT NULL AND flavor <> ''"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val quantity = cursor.getInt(4)
                val fix = fixFor(cursor.getString(2), cursor.getString(3), cursor.getDouble(5))
                    ?: continue
                fixes += PendingFix(
                    rowId = cursor.getLong(0),
                    orderId = cursor.getLong(1),
                    flavor = fix.first,
                    unitPrice = fix.second,
                    totalPrice = fix.second * quantity
                )
            }
        }
        if (fixes.isEmpty()) return 0

        fixes.forEach { fix ->
            db.execSQL(
                "UPDATE order_items SET flavor = ?, unitPrice = ?, totalPrice = ? WHERE id = ?",
                arrayOf(fix.flavor, fix.unitPrice, fix.totalPrice, fix.rowId)
            )
        }

        fixes.map { it.orderId }.distinct().forEach { orderId ->
            var subtotal = 0.0
            db.query(
                "SELECT totalPrice FROM order_items WHERE orderId = ?",
                arrayOf<Any>(orderId)
            ).use { cursor ->
                while (cursor.moveToNext()) subtotal += cursor.getDouble(0)
            }
            var discountLabel = ""
            db.query(
                "SELECT discountLabel FROM orders WHERE id = ?",
                arrayOf<Any>(orderId)
            ).use { cursor ->
                if (cursor.moveToNext()) discountLabel = cursor.getString(0) ?: ""
            }
            val deduction = deductionFor(discountLabel, subtotal)
            db.execSQL(
                "UPDATE orders SET subtotal = ?, discountDeduction = ?, total = ?, " +
                    "syncStatus = 'PENDING' WHERE id = ?",
                arrayOf(subtotal, deduction, (subtotal - deduction).coerceAtLeast(0.0), orderId)
            )
        }

        Log.i(TAG, "Repaired ${fixes.size} line items across ${fixes.map { it.orderId }.distinct().size} orders")
        return fixes.size
    }
}
