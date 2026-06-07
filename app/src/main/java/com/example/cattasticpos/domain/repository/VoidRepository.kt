package com.example.cattasticpos.domain.repository

import com.example.cattasticpos.domain.model.VoidRecord

interface VoidRepository {
    suspend fun saveVoidRecord(record: VoidRecord)
}
