package com.example.cattasticpos.domain.model

data class Expense(
    val id: String,
    val timestamp: Long,
    val description: String,
    val amount: Double,
    val recordedBy: String
)
