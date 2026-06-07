package com.example.cattasticpos.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.R
import kotlinx.coroutines.flow.first

class LowStockCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as CattasticPosApp).container
        val inventory = container.inventoryRepository.getAllInventory().first()
        val lowStockItems = inventory.filter { it.currentStock <= it.reorderThreshold }
        if (lowStockItems.isEmpty()) return Result.success()

        ensureChannel(applicationContext)
        val summary = lowStockItems.joinToString(", ") { "${it.itemName} (${it.currentStock} ${it.unit})" }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Low stock alert")
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "low_stock_check"
        private const val CHANNEL_ID = "inventory_alerts"
        private const val NOTIFICATION_ID = 1001

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Inventory Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                val manager = context.getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }
        }
    }
}
