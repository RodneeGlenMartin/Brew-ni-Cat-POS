package com.example.cattasticpos.domain.repository

import com.example.cattasticpos.domain.model.AppConfig
import com.example.cattasticpos.domain.model.Cashier
import com.example.cattasticpos.domain.model.GcashAccount
import kotlinx.coroutines.flow.Flow

interface AppConfigRepository {
    fun getAppConfig(): Flow<AppConfig?>
    suspend fun updateConfig(targetSales: Double, startingCashFloat: Double, pinHash: String)
    suspend fun updatePaymentConfig(cashiers: List<Cashier>, gcashAccounts: List<GcashAccount>)
}
