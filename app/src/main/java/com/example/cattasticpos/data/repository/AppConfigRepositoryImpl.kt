package com.example.cattasticpos.data.repository

import com.example.cattasticpos.data.local.dao.AppConfigDao
import com.example.cattasticpos.data.local.entity.AppConfigEntity
import com.example.cattasticpos.domain.model.AppConfig
import com.example.cattasticpos.domain.model.AppThemeAccent
import com.example.cattasticpos.domain.model.Cashier
import com.example.cattasticpos.domain.model.GcashAccount
import com.example.cattasticpos.domain.model.PaymentConfigJson
import com.example.cattasticpos.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class AppConfigRepositoryImpl(
    private val dao: AppConfigDao
) : AppConfigRepository {

    override fun getAppConfig(): Flow<AppConfig?> {
        return dao.getAppConfig().map { entity ->
            entity?.let { toAppConfig(it) }
        }
    }

    override suspend fun updateConfig(targetSales: Double, startingCashFloat: Double, pinHash: String) {
        val existing = dao.getAppConfigOnce()
        val paymentConfig = PaymentConfigJson.fromStoredJson(existing?.cashiersJson.orEmpty())
        dao.insertConfig(
            AppConfigEntity(
                id = 1,
                targetSales = targetSales,
                startingCashFloat = startingCashFloat,
                pinHash = pinHash,
                cashiersJson = paymentConfig.toStoredJson()
            )
        )
    }

    override suspend fun updatePaymentConfig(cashiers: List<Cashier>, gcashAccounts: List<GcashAccount>) {
        val existing = dao.getAppConfigOnce()
        val paymentConfig = PaymentConfigJson.fromStoredJson(existing?.cashiersJson.orEmpty())
        val activeId = paymentConfig.activeCashierId?.takeIf { id -> cashiers.any { it.id == id } }
            ?: cashiers.firstOrNull()?.id
        dao.insertConfig(
            AppConfigEntity(
                id = 1,
                targetSales = existing?.targetSales ?: AppConfigEntity.DEFAULT_TARGET_SALES,
                startingCashFloat = existing?.startingCashFloat ?: AppConfigEntity.DEFAULT_STARTING_CASH_FLOAT,
                pinHash = existing?.pinHash ?: AppConfig.DEFAULT_PIN_HASH,
                cashiersJson = PaymentConfigJson.toStoredJson(
                    cashiers = cashiers,
                    gcashAccounts = gcashAccounts,
                    themeAccentId = paymentConfig.themeAccentId,
                    activeCashierId = activeId
                )
            )
        )
    }

    override suspend fun updateThemeAccent(themeAccentId: String) {
        val existing = dao.getAppConfigOnce()
        val paymentConfig = PaymentConfigJson.fromStoredJson(existing?.cashiersJson.orEmpty())
        dao.insertConfig(
            AppConfigEntity(
                id = 1,
                targetSales = existing?.targetSales ?: AppConfigEntity.DEFAULT_TARGET_SALES,
                startingCashFloat = existing?.startingCashFloat ?: AppConfigEntity.DEFAULT_STARTING_CASH_FLOAT,
                pinHash = existing?.pinHash ?: AppConfig.DEFAULT_PIN_HASH,
                cashiersJson = paymentConfig.copy(themeAccentId = themeAccentId).toStoredJson()
            )
        )
    }

    override suspend fun updateActiveCashier(activeCashierId: String) {
        val existing = dao.getAppConfigOnce()
        val paymentConfig = PaymentConfigJson.fromStoredJson(existing?.cashiersJson.orEmpty())
        if (paymentConfig.cashiers.none { it.id == activeCashierId }) return
        dao.insertConfig(
            AppConfigEntity(
                id = 1,
                targetSales = existing?.targetSales ?: AppConfigEntity.DEFAULT_TARGET_SALES,
                startingCashFloat = existing?.startingCashFloat ?: AppConfigEntity.DEFAULT_STARTING_CASH_FLOAT,
                pinHash = existing?.pinHash ?: AppConfig.DEFAULT_PIN_HASH,
                cashiersJson = paymentConfig.copy(activeCashierId = activeCashierId).toStoredJson()
            )
        )
    }

    override suspend fun updateSyncConfig(supabaseUrl: String, supabaseAnonKey: String) {
        val existing = dao.getAppConfigOnce()
        if (existing != null) {
            dao.insertConfig(
                existing.copy(
                    supabaseUrl = supabaseUrl.trim(),
                    supabaseAnonKey = supabaseAnonKey.trim()
                )
            )
        }
    }

    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    private fun toAppConfig(entity: AppConfigEntity): AppConfig {
        val paymentConfig = PaymentConfigJson.fromStoredJson(entity.cashiersJson)
        val resolvedActiveId = paymentConfig.activeCashierId?.takeIf { id ->
            paymentConfig.cashiers.any { it.id == id }
        } ?: paymentConfig.cashiers.firstOrNull()?.id

        val defaultUrl = "https://hyeotyohpdpmmvquotnd.supabase.co"
        val defaultKey = "sb_publishable_orak9Nk7HGB_qFHgXMdIzA_11T8NfYQ"
        
        var shouldUpdate = false
        var updatedEntity = entity
        
        val actualDeviceId = if (entity.deviceId.isBlank()) {
            val newUuid = java.util.UUID.randomUUID().toString()
            updatedEntity = updatedEntity.copy(deviceId = newUuid)
            shouldUpdate = true
            newUuid
        } else {
            entity.deviceId
        }

        val actualUrl = if (entity.supabaseUrl.isBlank()) {
            updatedEntity = updatedEntity.copy(supabaseUrl = defaultUrl)
            shouldUpdate = true
            defaultUrl
        } else {
            entity.supabaseUrl
        }

        val actualKey = if (entity.supabaseAnonKey.isBlank()) {
            updatedEntity = updatedEntity.copy(supabaseAnonKey = defaultKey)
            shouldUpdate = true
            defaultKey
        } else {
            entity.supabaseAnonKey
        }

        if (shouldUpdate) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    dao.insertConfig(updatedEntity)
                } catch (e: Exception) {
                    android.util.Log.e("AppConfigRepositoryImpl", "Failed to update configs", e)
                }
            }
        }

        return AppConfig(
            targetSales = entity.targetSales,
            startingCashFloat = entity.startingCashFloat,
            pinHash = entity.pinHash,
            cashiers = paymentConfig.cashiers,
            gcashAccounts = paymentConfig.gcashAccounts,
            themeAccentId = paymentConfig.themeAccentId,
            activeCashierId = resolvedActiveId,
            supabaseUrl = actualUrl,
            supabaseAnonKey = actualKey,
            deviceId = actualDeviceId
        )
    }

    private fun PaymentConfigJson.toStoredJson(): String {
        return PaymentConfigJson.toStoredJson(
            cashiers = cashiers,
            gcashAccounts = gcashAccounts,
            themeAccentId = themeAccentId,
            activeCashierId = activeCashierId
        )
    }
}
