package com.example.cattasticpos.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.data.local.entity.OrderEntity
import com.example.cattasticpos.data.local.entity.OrderItemEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import androidx.room.withTransaction

class HistoricalPullWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "cattastic_historical_pull_worker"
        private const val TAG = "HistoricalPullWorker"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result {
        val app = applicationContext as CattasticPosApp
        val database = app.container.database
        val config = database.appConfigDao().getAppConfigOnce() ?: return Result.success()

        val supabaseUrl = config.supabaseUrl.trim()
        val supabaseKey = config.supabaseAnonKey.trim()
        val localDeviceId = config.deviceId.trim()

        if (supabaseUrl.isEmpty() || supabaseKey.isEmpty() || localDeviceId.isEmpty()) {
            Log.d(TAG, "Supabase sync not configured. Skipping historical pull.")
            return Result.success()
        }

        val prefs = app.getSharedPreferences("cattastic_sync_prefs", Context.MODE_PRIVATE)
        val isPulled = prefs.getBoolean("historical_pull_done", false)
        if (isPulled) {
            Log.d(TAG, "Historical pull already completed in past.")
            return Result.success()
        }

        try {
            var offset = 0
            val limit = 100
            var hasMore = true

            Log.i(TAG, "Starting historical pull from Supabase...")

            while (hasMore) {
                // Fetch orders along with order_items using select relation
                val url = "$supabaseUrl/rest/v1/orders?select=*,order_items(*)&order=timestamp.asc&limit=$limit&offset=$offset"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer $supabaseKey")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to fetch orders from Supabase: ${response.code}")
                    response.close()
                    return Result.retry()
                }

                val body = response.body?.string() ?: "[]"
                response.close()

                val jsonArray = JSONArray(body)
                if (jsonArray.length() == 0) {
                    hasMore = false
                    break
                }

                Log.d(TAG, "Received ${jsonArray.length()} orders at offset $offset.")

                for (i in 0 until jsonArray.length()) {
                    val orderJson = jsonArray.getJSONObject(i)
                    val remoteId = orderJson.getLong("id")
                    val remoteDeviceId = orderJson.getString("device_id")

                    // Skip local echoes if we find them, but let's check local DB first
                    val targetLocalId = if (remoteDeviceId == localDeviceId) {
                        // This order originally belongs to this device
                        // Local ID is remoteId % 1,000,000,000L
                        remoteId % 1_000_000_000L
                    } else {
                        // Remote order from another device
                        remoteId
                    }

                    // Check if order exists in Room
                    val existingOrder = database.orderDao().getOrderWithItems(targetLocalId)
                    if (existingOrder == null) {
                        // Insert OrderEntity
                        val orderEntity = OrderEntity(
                            id = targetLocalId,
                            timestamp = orderJson.getLong("timestamp"),
                            subtotal = orderJson.getDouble("subtotal"),
                            discountDeduction = orderJson.getDouble("discount_deduction"),
                            discountLabel = orderJson.getString("discount_label"),
                            total = orderJson.getDouble("total"),
                            paymentMethod = orderJson.getString("payment_method"),
                            paymentReference = if (orderJson.isNull("payment_reference")) null else orderJson.getString("payment_reference"),
                            cashierId = if (orderJson.isNull("cashier_id")) null else orderJson.getString("cashier_id"),
                            cashierName = if (orderJson.isNull("cashier_name")) null else orderJson.getString("cashier_name"),
                            tableLabel = if (orderJson.isNull("table_label")) null else orderJson.getString("table_label"),
                            isServed = orderJson.optBoolean("is_served", false),
                            deviceId = remoteDeviceId,
                            syncStatus = "SYNCED"
                        )

                        // Parse and Insert OrderItemEntity rows
                        val itemsArray = orderJson.getJSONArray("order_items")
                        val itemEntities = mutableListOf<OrderItemEntity>()
                        for (j in 0 until itemsArray.length()) {
                            val itemJson = itemsArray.getJSONObject(j)
                            itemEntities.add(
                                OrderItemEntity(
                                    orderId = targetLocalId,
                                    itemId = itemJson.getString("item_id"),
                                    itemName = itemJson.getString("item_name"),
                                    variantId = itemJson.getString("variant_id"),
                                    variantName = itemJson.getString("variant_name"),
                                    flavor = if (itemJson.isNull("flavor")) null else itemJson.getString("flavor"),
                                    quantity = itemJson.getInt("quantity"),
                                    unitPrice = itemJson.getDouble("unit_price"),
                                    totalPrice = itemJson.getDouble("total_price")
                                )
                            )
                        }

                        // Insert transactional
                        database.withTransaction {
                            database.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys=OFF") // Temporarily disable FK checks to insert manually-assigned IDs
                            try {
                                database.orderDao().insertOrderWithItems(orderEntity, itemEntities)
                            } finally {
                                database.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys=ON")
                            }
                        }
                        Log.d(TAG, "Synced remote order $targetLocalId locally.")
                    }
                }

                offset += limit
                if (jsonArray.length() < limit) {
                    hasMore = false
                }
            }

            prefs.edit().putBoolean("historical_pull_done", true).apply()
            Log.i(TAG, "Historical pull successfully completed!")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Historical pull error", e)
            return Result.retry()
        }
    }
}
