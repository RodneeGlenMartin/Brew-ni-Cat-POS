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

    private fun csvEscape(value: String): String {
        val prefix = if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            "'"
        } else {
            ""
        }
        val escaped = value.replace("\"", "\"\"")
        return "\"$prefix$escaped\""
    }

    suspend operator fun invoke(orders: List<Order>, expenses: List<Expense>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "CatTastic_Backup_$dateStr.csv"

            val csvContent = buildString {
                append("TYPE,ID,TIMESTAMP,DESCRIPTION_OR_ITEMS,SUBTOTAL,DISCOUNT,TOTAL,PAYMENT_METHOD,RECORDED_BY\n")

                // Append Orders
                for (order in orders) {
                    val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(order.timestamp))
                    val itemsStr = order.items.joinToString(" | ") { "${it.quantity}x ${it.itemName}" }
                    append("ORDER,${csvEscape(order.id)},$timestampStr,${csvEscape(itemsStr)},${order.subtotal},${order.discountDeduction},${order.total},${csvEscape(order.paymentMethod)},\n")
                }

                // Append Expenses
                for (expense in expenses) {
                    val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(expense.timestamp))
                    append("EXPENSE,${csvEscape(expense.id)},$timestampStr,${csvEscape(expense.description)},0.0,0.0,${expense.amount},CASH,${csvEscape(expense.recordedBy)}\n")
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
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
            } else {
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: return@withContext Result.failure(Exception("No app storage available for export"))
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val file = java.io.File(downloadsDir, filename)
                file.writeText(csvContent)
            }

            Result.success(filename)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
