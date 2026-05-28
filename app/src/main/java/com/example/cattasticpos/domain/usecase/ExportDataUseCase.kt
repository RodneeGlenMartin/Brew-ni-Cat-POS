package com.example.cattasticpos.domain.usecase

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.example.cattasticpos.domain.model.Expense
import com.example.cattasticpos.domain.model.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportDataUseCase(private val context: Context) {

    suspend operator fun invoke(orders: List<Order>, expenses: List<Expense>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "CatTastic_Backup_$dateStr.csv"

            val csvContent = buildString {
                append("TYPE,ID,TIMESTAMP,DESCRIPTION_OR_ITEMS,SUBTOTAL,DISCOUNT,TOTAL,PAYMENT_METHOD,RECORDED_BY\n")

                // Append Orders
                for (order in orders) {
                    val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(order.timestamp))
                    val itemsStr = order.items.joinToString(" | ") { "${it.quantity}x ${it.itemName}" }.replace(",", "")
                    append("ORDER,${order.id},$timestampStr,$itemsStr,${order.subtotal},${order.discountDeduction},${order.total},${order.paymentMethod},\n")
                }

                // Append Expenses
                for (expense in expenses) {
                    val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(expense.timestamp))
                    append("EXPENSE,${expense.id},$timestampStr,${expense.description.replace(",", "")},0.0,0.0,${expense.amount},CASH,${expense.recordedBy}\n")
                }
            }

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvContent.toByteArray())
            } ?: return@withContext Result.failure(Exception("Failed to open output stream"))

            Result.success(filename)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
