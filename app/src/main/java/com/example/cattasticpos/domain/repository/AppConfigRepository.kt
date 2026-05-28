package com.example.cattasticpos.domain.repository

import com.example.cattasticpos.domain.model.AppConfig
import kotlinx.coroutines.flow.Flow

interface AppConfigRepository {
    fun getAppConfig(): Flow<AppConfig?>
    suspend fun updateConfig(targetSales: Double, startingCashFloat: Double, pinHash: String)
}
