package com.example.cattasticpos.data.repository

import com.example.cattasticpos.data.local.dao.VoidDao
import com.example.cattasticpos.data.local.entity.VoidRecordEntity
import com.example.cattasticpos.domain.model.VoidRecord
import com.example.cattasticpos.domain.repository.VoidRepository

class VoidRepositoryImpl(
    private val voidDao: VoidDao
) : VoidRepository {
    override suspend fun saveVoidRecord(record: VoidRecord) {
        voidDao.insertVoidRecord(
            VoidRecordEntity(
                id = record.id,
                orderId = record.orderId,
                reason = record.reason,
                timestamp = record.timestamp,
                cashierId = record.cashierId,
                orderTotal = record.orderTotal
            )
        )
    }
}
