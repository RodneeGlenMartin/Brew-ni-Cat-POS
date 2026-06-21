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

                val recipeRepository = app.container.recipeRepository
                val inventoryRepository = app.container.inventoryRepository
                for (i in 0 until jsonArray.length()) {
                    // Bulk historical pull: do NOT restock on voids — those already happened and
                    // the corrected stock arrives via inventory sync.
                    com.example.cattasticpos.data.sync.OrderSyncMerger.mergeRemoteOrder(
                        database = database,
                        recipeRepository = recipeRepository,
                        inventoryRepository = inventoryRepository,
                        orderJson = jsonArray.getJSONObject(i),
                        localDeviceId = localDeviceId,
                        restoreInventoryOnVoid = false
                    )
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
