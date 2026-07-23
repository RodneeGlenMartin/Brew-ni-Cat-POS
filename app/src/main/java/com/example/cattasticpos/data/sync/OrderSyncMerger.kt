package com.example.cattasticpos.data.sync

import android.util.Log
import androidx.room.withTransaction
import com.example.cattasticpos.data.local.PosDatabase
import com.example.cattasticpos.data.local.entity.OrderEntity
import com.example.cattasticpos.data.local.entity.OrderItemEntity
import com.example.cattasticpos.domain.model.OrderItem
import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.domain.repository.RecipeRepository
import com.example.cattasticpos.domain.usecase.InventoryRestorationHelper
import org.json.JSONObject

/**
 * Single source of truth for turning a cloud order record into local Room state.
 *
 * Every downloader (the realtime websocket, the one-time historical pull, and the periodic
 * catch-up in [com.example.cattasticpos.worker.SyncWorker]) funnels through here so they can
 * never disagree on how a remote order maps to a local row.
 *
 * Key invariant: downloaded orders are inserted as brand-new LOCAL rows with an
 * autoincrement primary key and are deduplicated on [OrderEntity.remoteId] (the globally
 * unique Supabase id). We never insert a foreign order under a manually-chosen id, so two
 * devices that both created "order #5" can no longer collide, and the local autoincrement
 * sequence is never poisoned by large foreign ids.
 */
object OrderSyncMerger {

    private const val TAG = "OrderSyncMerger"
    private const val DEVICE_PARTITION = 1_000_000_000L

    /**
     * Merge a single cloud order (which must include its nested `order_items` array) into Room.
     *
     * @param restoreInventoryOnVoid when true (live realtime events) inventory is restocked the
     *   first time we observe a remote void. For bulk/catch-up syncs this must be false: those
     *   voids already happened elsewhere and the corrected stock arrives via inventory sync, so
     *   restoring again would double-count.
     */
    suspend fun mergeRemoteOrder(
        database: PosDatabase,
        recipeRepository: RecipeRepository,
        inventoryRepository: InventoryRepository,
        orderJson: JSONObject,
        localDeviceId: String,
        restoreInventoryOnVoid: Boolean
    ) {
        val dao = database.orderDao()
        val remoteId = orderJson.getLong("id")
        val remoteDeviceId = orderJson.getString("device_id")
        val isVoided = orderJson.optBoolean("is_voided", false)
        val isServed = orderJson.optBoolean("is_served", false)
        val now = System.currentTimeMillis()

        // Locate any existing local copy: first by remoteId, then (for our own orders uploaded
        // before remoteId existed) by the legacy stripped id, backfilling remoteId when found.
        var existing = dao.getOrderByRemoteId(remoteId)
        if (existing == null && remoteDeviceId == localDeviceId) {
            val legacy = dao.getOrderWithItems(remoteId % DEVICE_PARTITION)
            if (legacy != null && legacy.order.remoteId == null) {
                dao.setRemoteId(legacy.order.id, remoteId)
                existing = dao.getOrderWithItems(legacy.order.id)
            }
        }

        if (existing != null) {
            val local = existing.order
            // Cloud is authoritative for status. Restock once, only for live voids.
            if (isVoided && !local.isVoided && restoreInventoryOnVoid) {
                runCatching {
                    InventoryRestorationHelper.restoreForOrderItems(
                        existing.items.map { it.toDomain() },
                        recipeRepository,
                        inventoryRepository
                    )
                }.onFailure { Log.e(TAG, "Inventory restore on remote void failed for $remoteId", it) }
            }
            if (local.isVoided != isVoided ||
                local.isServed != isServed ||
                local.syncStatus != "SYNCED" ||
                local.remoteId != remoteId
            ) {
                dao.updateOrderEntity(
                    local.copy(
                        isVoided = isVoided,
                        isServed = isServed,
                        syncStatus = "SYNCED",
                        lastSyncedAt = now,
                        remoteId = remoteId
                    )
                )
            }
            return
        }

        // Brand-new order from the cloud → insert as a fresh local row (id = 0 -> autoincrement).
        val orderEntity = OrderEntity(
            id = 0,
            timestamp = orderJson.getLong("timestamp"),
            subtotal = orderJson.getDouble("subtotal"),
            discountDeduction = orderJson.getDouble("discount_deduction"),
            discountLabel = orderJson.optString("discount_label", ""),
            total = orderJson.getDouble("total"),
            paymentMethod = orderJson.getString("payment_method"),
            paymentReference = orderJson.optStringOrNull("payment_reference"),
            cashierId = orderJson.optStringOrNull("cashier_id"),
            cashierName = orderJson.optStringOrNull("cashier_name"),
            tableLabel = orderJson.optStringOrNull("table_label"),
            isServed = isServed,
            deviceId = remoteDeviceId,
            syncStatus = "SYNCED",
            isVoided = isVoided,
            lastSyncedAt = now,
            remoteId = remoteId
        )

        val itemEntities = mutableListOf<OrderItemEntity>()
        val itemsArray = orderJson.optJSONArray("order_items")
        if (itemsArray != null) {
            for (j in 0 until itemsArray.length()) {
                val itemJson = itemsArray.getJSONObject(j)
                itemEntities.add(
                    OrderItemEntity(
                        id = 0,
                        orderId = 0,
                        itemId = itemJson.getString("item_id"),
                        itemName = itemJson.getString("item_name"),
                        variantId = itemJson.getString("variant_id"),
                        variantName = itemJson.getString("variant_name"),
                        flavor = itemJson.optStringOrNull("flavor"),
                        quantity = itemJson.getInt("quantity"),
                        unitPrice = itemJson.getDouble("unit_price"),
                        totalPrice = itemJson.getDouble("total_price")
                    )
                )
            }
        }

        database.withTransaction {
            dao.insertOrderWithItems(orderEntity, itemEntities)
        }
        Log.d(TAG, "Inserted remote order remoteId=$remoteId (device $remoteDeviceId) as new local row")
    }

    private fun OrderItemEntity.toDomain(): OrderItem = OrderItem(
        id = id,
        orderId = orderId,
        itemId = itemId,
        itemName = itemName,
        variantId = variantId,
        variantName = variantName,
        flavor = flavor,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice
    )
}

internal fun JSONObject.optStringOrNull(key: String): String? =
    if (!has(key) || isNull(key)) null else optString(key).takeUnless { it.isEmpty() }
