package com.example.cattasticpos.data.repository

import com.example.cattasticpos.data.local.dao.AppConfigDao
import com.example.cattasticpos.data.local.entity.AppConfigEntity
import com.example.cattasticpos.domain.model.AppConfig
import com.example.cattasticpos.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppConfigRepositoryImpl(
    private val dao: AppConfigDao
) : AppConfigRepository {

    override fun getAppConfig(): Flow<AppConfig?> {
        return dao.getAppConfig().map { entity ->
            entity?.let {
                AppConfig(targetSales = it.targetSales, startingCashFloat = it.startingCashFloat, pinHash = it.pinHash)
            }
        }
    }

    override suspend fun updateConfig(targetSales: Double, startingCashFloat: Double, pinHash: String) {
        dao.updateConfig(
            AppConfigEntity(
                id = 1,
                targetSales = targetSales,
                startingCashFloat = startingCashFloat,
                pinHash = pinHash
            )
        )
    }
}
