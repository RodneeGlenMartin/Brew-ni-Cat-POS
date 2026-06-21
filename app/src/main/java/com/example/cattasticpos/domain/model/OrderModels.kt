package com.example.cattasticpos.domain.model

import java.util.Locale

data class Order(
    val id: Long = 0,
    val timestamp: Long,
    val subtotal: Double,
    val discountDeduction: Double,
    val discountLabel: String,
    val total: Double,
    val paymentMethod: String,
    val paymentReference: String?,
    val cashierId: String? = null,
    val cashierName: String? = null,
    val tableLabel: String? = null,
    val isServed: Boolean = false,
    val deviceId: String = "",
    val syncStatus: String = "PENDING",
    val isVoided: Boolean = false,
    val lastSyncedAt: Long = 0,
    val items: List<OrderItem>
) {
    val receiptNumber: String
        get() {
            if (id <= 0L) return "----"
            val displayId = if (id >= 1_000_000_000L) id % 1_000_000_000L else id
            return String.format(Locale.US, "%04d", displayId)
        }
}

data class OrderItem(
    val id: Long,
    val orderId: Long,
    val itemId: String,
    val itemName: String,
    val variantId: String,
    val variantName: String,
    val flavor: String?,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)
