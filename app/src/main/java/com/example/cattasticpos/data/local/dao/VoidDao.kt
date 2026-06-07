package com.example.cattasticpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cattasticpos.data.local.entity.VoidRecordEntity

@Dao
interface VoidDao {
    @Insert
    suspend fun insertVoidRecord(record: VoidRecordEntity)

    @Query("SELECT * FROM void_records ORDER BY timestamp DESC")
    suspend fun getAllVoidRecords(): List<VoidRecordEntity>
}
