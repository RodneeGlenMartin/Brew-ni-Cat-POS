package com.example.cattasticpos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val targetSales: Double,
    val startingCashFloat: Double,
    val pinHash: String = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"
)
