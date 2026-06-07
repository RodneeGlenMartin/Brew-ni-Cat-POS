package com.example.cattasticpos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "void_records")
data class VoidRecordEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val reason: String,
    val timestamp: Long,
    val cashierId: String?,
    val orderTotal: Double
)
