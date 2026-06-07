package com.example.cattasticpos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val targetSales: Double,
    val startingCashFloat: Double,
    val pinHash: String,
    val cashiersJson: String = DEFAULT_CASHIERS_JSON
) {
    companion object {
        const val DEFAULT_CASHIERS_JSON =
            """[{"id":"cashier_default","name":"Popot","pinHash":"otCBSIxSZkk6vcF7SKwqCw==:Seyex1KVzCA7gLC3+1Vi8AHYtjU7A168GCGRihADbp0="}]"""
    }
}
