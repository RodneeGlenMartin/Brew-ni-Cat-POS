package com.example.cattasticpos.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.data.local.entity.OrderEntity
import com.example.cattasticpos.data.local.entity.OrderItemEntity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.first

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "cattastic_sync_worker"
        private const val TAG = "SyncWorker"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        fun getSupabaseOrderId(deviceId: String, localId: Long): Long {
            val deviceHash = deviceId.hashCode().absoluteValue % 1_000_000L
            return (deviceHash * 1_000_000_000L) + localId
        }

        fun getSupabaseOrderItemId(supabaseOrderId: Long, itemIndex: Int): Long {
            return (supabaseOrderId * 1000L) + itemIndex
        }

        fun triggerImmediateSync(context: Context) {
            val request = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>().build()
            androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
                "immediate_sync_upload",
                androidx.work.ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result {
        val app = applicationContext as CattasticPosApp
        val database = app.container.database
        val config = database.appConfigDao().getAppConfigOnce() ?: return Result.success()

        val supabaseUrl = config.supabaseUrl.trim()
        val supabaseKey = config.supabaseAnonKey.trim()
        val deviceId = config.deviceId.trim()

        if (supabaseUrl.isEmpty() || supabaseKey.isEmpty() || deviceId.isEmpty()) {
            Log.d(TAG, "Supabase sync not configured. Skipping.")
            return Result.success()
        }

        try {
            // Sync Catalog: Categories, Items, Inventory, and Recipe Mappings (Two-Way Sync)
            try {
                // 1. Upload Local Categories
                val categories = database.menuDao().getCategories().first()
                if (categories.isNotEmpty()) {
                    val catArray = JSONArray()
                    categories.forEach { cat ->
                        val catJson = JSONObject().apply {
                            put("id", cat.id)
                            put("name", cat.name)
                        }
                        catArray.put(catJson)
                    }
                    val catRequest = Request.Builder()
                        .url("$supabaseUrl/rest/v1/categories")
                        .post(catArray.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .header("apikey", supabaseKey)
                        .header("Authorization", "Bearer $supabaseKey")
                        .header("Prefer", "resolution=merge-duplicates")
                        .header("Content-Type", "application/json")
                        .build()
                    client.newCall(catRequest).execute().close()
                }

                // 2. Upload Local Items
                val items = database.menuDao().getItems().first()
                if (items.isNotEmpty()) {
                    val itemsArray = JSONArray()
                    items.forEach { item ->
                        val itemJson = JSONObject().apply {
                            put("id", item.id)
                            put("category_id", item.categoryId)
                            put("name", item.name)
                            put("flavors", item.flavors)
                            put("variants_json", item.variantsJson)
                            put("is_available", true)
                        }
                        itemsArray.put(itemJson)
                    }
                    val itemsRequest = Request.Builder()
                        .url("$supabaseUrl/rest/v1/items")
                        .post(itemsArray.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .header("apikey", supabaseKey)
                        .header("Authorization", "Bearer $supabaseKey")
                        .header("Prefer", "resolution=merge-duplicates")
                        .header("Content-Type", "application/json")
                        .build()
                    client.newCall(itemsRequest).execute().close()
                }

                // 3. Upload Local Inventory
                val inventory = database.inventoryDao().getAllInventory().first()
                if (inventory.isNotEmpty()) {
                    val invArray = JSONArray()
                    inventory.forEach { inv ->
                        val invJson = JSONObject().apply {
                            put("id", inv.id)
                            put("item_name", inv.itemName)
                            put("unit", inv.unit)
                            put("current_stock", inv.currentStock)
                            put("reorder_threshold", inv.reorderThreshold)
                        }
                        invArray.put(invJson)
                    }
                    val invRequest = Request.Builder()
                        .url("$supabaseUrl/rest/v1/inventory")
                        .post(invArray.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .header("apikey", supabaseKey)
                        .header("Authorization", "Bearer $supabaseKey")
                        .header("Prefer", "resolution=merge-duplicates")
                        .header("Content-Type", "application/json")
                        .build()
                    client.newCall(invRequest).execute().close()
                }

                // 4. Upload Local Recipe Mappings
                val recipes = database.recipeDao().getAllMappingsOnce()
                if (recipes.isNotEmpty()) {
                    val recArray = JSONArray()
                    recipes.forEach { rec ->
                        val recJson = JSONObject().apply {
                            put("id", rec.id)
                            put("menu_item_id", rec.menuItemId)
                            put("size_variant_name", rec.variantName ?: JSONObject.NULL)
                            put("inventory_item_id", rec.inventoryItemId)
                            put("deduction_quantity", rec.deductionQuantity)
                        }
                        recArray.put(recJson)
                    }
                    val recRequest = Request.Builder()
                        .url("$supabaseUrl/rest/v1/recipe_mappings")
                        .post(recArray.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .header("apikey", supabaseKey)
                        .header("Authorization", "Bearer $supabaseKey")
                        .header("Prefer", "resolution=merge-duplicates")
                        .header("Content-Type", "application/json")
                        .build()
                    client.newCall(recRequest).execute().close()
                }

                // ==========================================
                // DOWNLOAD & SYNC CLOUD CHANGES TO DEVICE
                // ==========================================

                // A. Download & Sync Categories
                val getCatRequest = Request.Builder()
                    .url("$supabaseUrl/rest/v1/categories")
                    .get()
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer $supabaseKey")
                    .build()
                client.newCall(getCatRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val arr = JSONArray(body)
                            val downloadedList = mutableListOf<com.example.cattasticpos.data.local.entity.CategoryEntity>()
                            val downloadedIds = mutableSetOf<String>()
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                val id = obj.getString("id")
                                val name = obj.getString("name")
                                downloadedList.add(com.example.cattasticpos.data.local.entity.CategoryEntity(id, name))
                                downloadedIds.add(id)
                            }
                            if (downloadedList.isNotEmpty()) {
                                database.menuDao().insertCategories(downloadedList)
                                val localCats = database.menuDao().getCategories().first()
                                val toDelete = localCats.filter { it.id !in downloadedIds }.map { it.id }
                                if (toDelete.isNotEmpty()) {
                                    database.menuDao().deleteCategoriesByIds(toDelete)
                                }
                            }
                        }
                    }
                }

                // B. Download & Sync Items
                val getItemsRequest = Request.Builder()
                    .url("$supabaseUrl/rest/v1/items")
                    .get()
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer $supabaseKey")
                    .build()
                client.newCall(getItemsRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val arr = JSONArray(body)
                            val downloadedList = mutableListOf<com.example.cattasticpos.data.local.entity.ItemEntity>()
                            val downloadedIds = mutableSetOf<String>()
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                val id = obj.getString("id")
                                val categoryId = obj.getString("category_id")
                                val name = obj.getString("name")
                                val flavors = obj.optString("flavors", "")
                                val variantsJson = obj.optString("variants_json", "[]")
                                downloadedList.add(com.example.cattasticpos.data.local.entity.ItemEntity(id, categoryId, name, flavors, variantsJson))
                                downloadedIds.add(id)
                            }
                            if (downloadedList.isNotEmpty()) {
                                database.menuDao().insertItems(downloadedList)
                                val localItems = database.menuDao().getItems().first()
                                val toDelete = localItems.filter { it.id !in downloadedIds }.map { it.id }
                                if (toDelete.isNotEmpty()) {
                                    database.menuDao().deleteItemsByIds(toDelete)
                                }
                            }
                        }
                    }
                }

                // C. Download & Sync Inventory (Stock)
                val getInvRequest = Request.Builder()
                    .url("$supabaseUrl/rest/v1/inventory")
                    .get()
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer $supabaseKey")
                    .build()
                client.newCall(getInvRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val arr = JSONArray(body)
                            val downloadedList = mutableListOf<com.example.cattasticpos.data.local.entity.InventoryEntity>()
                            val downloadedIds = mutableSetOf<String>()
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                val id = obj.getString("id")
                                val itemName = obj.getString("item_name")
                                val unit = obj.getString("unit")
                                val currentStock = obj.getDouble("current_stock")
                                val reorderThreshold = obj.getDouble("reorder_threshold")
                                downloadedList.add(com.example.cattasticpos.data.local.entity.InventoryEntity(id, itemName, unit, currentStock, reorderThreshold))
                                downloadedIds.add(id)
                            }
                            if (downloadedList.isNotEmpty()) {
                                database.inventoryDao().insertInventoryItems(downloadedList)
                                val localInv = database.inventoryDao().getAllInventory().first()
                                val toDelete = localInv.filter { it.id !in downloadedIds }.map { it.id }
                                if (toDelete.isNotEmpty()) {
                                    database.inventoryDao().deleteInventoryItemsByIds(toDelete)
                                }
                            }
                        }
                    }
                }

                // D. Download & Sync Recipe Mappings
                val getRecRequest = Request.Builder()
                    .url("$supabaseUrl/rest/v1/recipe_mappings")
                    .get()
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer $supabaseKey")
                    .build()
                client.newCall(getRecRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val arr = JSONArray(body)
                            val downloadedList = mutableListOf<com.example.cattasticpos.data.local.entity.RecipeMappingEntity>()
                            val downloadedIds = mutableSetOf<String>()
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                val id = obj.getString("id")
                                val menuItemId = obj.getString("menu_item_id")
                                val sizeVariantName = if (obj.isNull("size_variant_name")) null else obj.getString("size_variant_name")
                                val inventoryItemId = obj.getString("inventory_item_id")
                                val deductionQuantity = obj.getDouble("deduction_quantity")
                                downloadedList.add(com.example.cattasticpos.data.local.entity.RecipeMappingEntity(id, menuItemId, sizeVariantName, inventoryItemId, deductionQuantity))
                                downloadedIds.add(id)
                            }
                            if (downloadedList.isNotEmpty()) {
                                database.recipeDao().insertMappings(downloadedList)
                                val localRecipes = database.recipeDao().getAllMappingsOnce()
                                val toDelete = localRecipes.filter { it.id !in downloadedIds }.map { it.id }
                                if (toDelete.isNotEmpty()) {
                                    database.recipeDao().deleteMappingsByIds(toDelete)
                                }
                            }
                        }
                    }
                }
            } catch (ce: Exception) {
                Log.e(TAG, "Catalog sync error", ce)
            }

            // ==========================================
            // UPLOAD: Push Local Pending Orders to Cloud
            // ==========================================
            try {
                val pendingOrders = database.orderDao().getPendingSyncOrders()

                if (pendingOrders.isNotEmpty()) {
                    Log.i(TAG, "Found ${pendingOrders.size} pending orders to upload.")

                    for (orderWithItems in pendingOrders) {
                        val order = orderWithItems.order
                        val items = orderWithItems.items

                        if (order.remoteId != null) {
                            // Already present in the cloud (an own order re-synced, or a foreign
                            // order voided locally). Only push the mutable status fields so we
                            // never clobber the original device_id / totals / item rows.
                            val patchJson = JSONObject().apply {
                                put("is_voided", order.isVoided)
                                put("is_served", order.isServed)
                            }
                            val patchRequest = Request.Builder()
                                .url("$supabaseUrl/rest/v1/orders?id=eq.${order.remoteId}")
                                .patch(patchJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                                .header("apikey", supabaseKey)
                                .header("Authorization", "Bearer $supabaseKey")
                                .header("Content-Type", "application/json")
                                .build()
                            val patchResponse = client.newCall(patchRequest).execute()
                            if (!patchResponse.isSuccessful) {
                                val body = patchResponse.body?.string() ?: ""
                                Log.e(TAG, "Failed to patch order ${order.id} (remote ${order.remoteId}): ${patchResponse.code} - $body")
                                patchResponse.close()
                                continue
                            }
                            patchResponse.close()

                            database.orderDao().updateOrderEntity(
                                order.copy(syncStatus = "SYNCED", lastSyncedAt = System.currentTimeMillis())
                            )
                            Log.d(TAG, "Patched order ${order.id} status to cloud (remote ${order.remoteId}).")
                            continue
                        }

                        // Brand-new own order -> POST full record under a globally-unique id.
                        val supabaseOrderId = getSupabaseOrderId(deviceId, order.id)

                        val orderJson = JSONObject().apply {
                            put("id", supabaseOrderId)
                            put("device_id", deviceId)
                            put("timestamp", order.timestamp)
                            put("subtotal", order.subtotal)
                            put("discount_deduction", order.discountDeduction)
                            put("discount_label", order.discountLabel)
                            put("total", order.total)
                            put("payment_method", order.paymentMethod)
                            put("payment_reference", order.paymentReference ?: JSONObject.NULL)
                            put("cashier_id", order.cashierId ?: JSONObject.NULL)
                            put("cashier_name", order.cashierName ?: JSONObject.NULL)
                            put("table_label", order.tableLabel ?: JSONObject.NULL)
                            put("is_served", order.isServed)
                            put("is_voided", order.isVoided)
                        }

                        val orderRequest = Request.Builder()
                            .url("$supabaseUrl/rest/v1/orders")
                            .post(orderJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                            .header("apikey", supabaseKey)
                            .header("Authorization", "Bearer $supabaseKey")
                            .header("Prefer", "resolution=merge-duplicates")
                            .header("Content-Type", "application/json")
                            .build()

                        val orderResponse = client.newCall(orderRequest).execute()
                        if (!orderResponse.isSuccessful) {
                            val body = orderResponse.body?.string() ?: ""
                            Log.e(TAG, "Failed to upload order header ${order.id}: ${orderResponse.code} - $body")
                            orderResponse.close()
                            continue
                        }
                        orderResponse.close()

                        if (items.isNotEmpty()) {
                            val itemsArray = JSONArray()
                            items.forEachIndexed { index, item ->
                                val itemJson = JSONObject().apply {
                                    put("id", getSupabaseOrderItemId(supabaseOrderId, index))
                                    put("order_id", supabaseOrderId)
                                    put("item_id", item.itemId)
                                    put("item_name", item.itemName)
                                    put("variant_id", item.variantId)
                                    put("variant_name", item.variantName)
                                    put("flavor", item.flavor ?: JSONObject.NULL)
                                    put("quantity", item.quantity)
                                    put("unit_price", item.unitPrice)
                                    put("total_price", item.totalPrice)
                                }
                                itemsArray.put(itemJson)
                            }

                            val itemsRequest = Request.Builder()
                                .url("$supabaseUrl/rest/v1/order_items")
                                .post(itemsArray.toString().toRequestBody(JSON_MEDIA_TYPE))
                                .header("apikey", supabaseKey)
                                .header("Authorization", "Bearer $supabaseKey")
                                .header("Prefer", "resolution=merge-duplicates")
                                .header("Content-Type", "application/json")
                                .build()

                            val itemsResponse = client.newCall(itemsRequest).execute()
                            if (!itemsResponse.isSuccessful) {
                                val body = itemsResponse.body?.string() ?: ""
                                Log.e(TAG, "Failed to upload order items for order ${order.id}: ${itemsResponse.code} - $body")
                                itemsResponse.close()
                                continue
                            }
                            itemsResponse.close()
                        }

                        // Mark SYNCED and remember the global id so future syncs PATCH, not re-POST.
                        database.orderDao().updateOrderEntity(
                            order.copy(
                                syncStatus = "SYNCED",
                                lastSyncedAt = System.currentTimeMillis(),
                                remoteId = supabaseOrderId
                            )
                        )
                        Log.d(TAG, "Uploaded new order ${order.id} (Supabase ID: $supabaseOrderId)")
                    }
                } else {
                    Log.d(TAG, "No pending orders to upload.")
                }
            } catch (oe: Exception) {
                Log.e(TAG, "Order upload error", oe)
            }

            // ==========================================
            // DOWNLOAD: Catch-up sync for orders created/voided on other devices
            // (or while this device was offline). Merges through OrderSyncMerger so the
            // local-id mapping is identical to the realtime and historical-pull paths.
            // ==========================================
            try {
                Log.d(TAG, "Starting catch-up order sync from Supabase...")
                val getOrdersRequest = Request.Builder()
                    .url("$supabaseUrl/rest/v1/orders?select=*,order_items(*)&order=timestamp.desc&limit=500")
                    .get()
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer $supabaseKey")
                    .build()

                client.newCall(getOrdersRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val arr = JSONArray(body)
                            Log.d(TAG, "Catch-up: examining ${arr.length()} cloud orders")
                            val recipeRepository = app.container.recipeRepository
                            val inventoryRepository = app.container.inventoryRepository
                            for (i in 0 until arr.length()) {
                                com.example.cattasticpos.data.sync.OrderSyncMerger.mergeRemoteOrder(
                                    database = database,
                                    recipeRepository = recipeRepository,
                                    inventoryRepository = inventoryRepository,
                                    orderJson = arr.getJSONObject(i),
                                    localDeviceId = deviceId,
                                    restoreInventoryOnVoid = false
                                )
                            }
                        }
                    } else {
                        Log.e(TAG, "Failed to download orders: ${response.code}")
                    }
                }
            } catch (he: Exception) {
                Log.e(TAG, "Catch-up sync error", he)
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            return Result.retry()
        }
    }
}
