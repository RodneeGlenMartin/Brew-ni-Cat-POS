package com.example.cattasticpos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val subtotal: Double,
    val discountDeduction: Double,
    val discountLabel: String,
    val total: Double,
    val paymentMethod: String,
    val paymentReference: String?
)
