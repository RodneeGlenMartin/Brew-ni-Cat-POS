package com.example.cattasticpos.data.repository

import com.example.cattasticpos.data.local.dao.AppConfigDao
import com.example.cattasticpos.data.local.entity.AppConfigEntity
import com.example.cattasticpos.domain.model.AppConfig
import com.example.cattasticpos.domain.model.Cashier
import com.example.cattasticpos.domain.model.GcashAccount
import com.example.cattasticpos.domain.model.PaymentConfigJson
import com.example.cattasticpos.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppConfigRepositoryImpl(
    private val dao: AppConfigDao
) : AppConfigRepository {

    override fun getAppConfig(): Flow<AppConfig?> {
        return dao.getAppConfig().map { entity ->
            entity?.let {
                val paymentConfig = PaymentConfigJson.fromStoredJson(it.cashiersJson)
                AppConfig(
                    targetSales = it.targetSales,
                    startingCashFloat = it.startingCashFloat,
                    pinHash = it.pinHash,
                    cashiers = paymentConfig.cashiers,
                    gcashAccounts = paymentConfig.gcashAccounts
                )
            }
        }
    }

    override suspend fun updateConfig(targetSales: Double, startingCashFloat: Double, pinHash: String) {
        val existing = dao.getAppConfigOnce()
        val paymentConfig = PaymentConfigJson.fromStoredJson(existing?.cashiersJson.orEmpty())
        dao.updateConfig(
            AppConfigEntity(
                id = 1,
                targetSales = targetSales,
                startingCashFloat = startingCashFloat,
                pinHash = pinHash,
                cashiersJson = PaymentConfigJson.toStoredJson(
                    paymentConfig.cashiers,
                    paymentConfig.gcashAccounts
                )
            )
        )
    }

    override suspend fun updatePaymentConfig(cashiers: List<Cashier>, gcashAccounts: List<GcashAccount>) {
        val existing = dao.getAppConfigOnce()
        dao.updateConfig(
            AppConfigEntity(
                id = 1,
                targetSales = existing?.targetSales ?: 5000.0,
                startingCashFloat = existing?.startingCashFloat ?: 500.0,
                pinHash = existing?.pinHash ?: AppConfig.DEFAULT_PIN_HASH,
                cashiersJson = PaymentConfigJson.toStoredJson(cashiers, gcashAccounts)
            )
        )
    }
}
