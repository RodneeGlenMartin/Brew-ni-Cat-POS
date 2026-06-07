package com.example.cattasticpos.domain.model

data class VoidRecord(
    val id: String,
    val orderId: String,
    val reason: String,
    val timestamp: Long,
    val cashierId: String?,
    val orderTotal: Double
)
