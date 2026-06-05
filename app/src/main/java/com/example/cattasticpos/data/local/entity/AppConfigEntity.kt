package com.example.cattasticpos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val targetSales: Double,
    val startingCashFloat: Double,
    val pinHash: String
)
