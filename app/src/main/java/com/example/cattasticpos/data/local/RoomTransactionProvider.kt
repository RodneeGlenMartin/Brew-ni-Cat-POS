package com.example.cattasticpos.data.local

import androidx.room.withTransaction
import com.example.cattasticpos.domain.repository.TransactionProvider

class RoomTransactionProvider(private val db: PosDatabase) : TransactionProvider {
    override suspend fun <T> runAsTransaction(block: suspend () -> T): T {
        return db.withTransaction {
            block()
        }
    }
}
