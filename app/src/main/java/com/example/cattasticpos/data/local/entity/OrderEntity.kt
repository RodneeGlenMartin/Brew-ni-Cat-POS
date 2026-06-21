package com.example.cattasticpos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["remoteId"], unique = true)
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    // Global Supabase order id this row mirrors. Null for local orders not yet uploaded.
    // Downloaded orders (own or from other devices) keep their real local autoincrement `id`
    // and are deduplicated on this column, so cross-device ids never collide and the local
    // autoincrement sequence is never poisoned by foreign ids.
    val remoteId: Long? = null
)
