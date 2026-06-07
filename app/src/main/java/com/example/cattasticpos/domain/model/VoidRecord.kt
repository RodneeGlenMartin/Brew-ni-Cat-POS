package com.example.cattasticpos.domain.model

data class VoidRecord(
    val id: String,
    val orderId: Long,
    val reason: String,
    val timestamp: Long,
    val cashierId: String?,
    val orderTotal: Double
)
