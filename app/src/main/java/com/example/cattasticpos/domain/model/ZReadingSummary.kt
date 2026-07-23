package com.example.cattasticpos.domain.model

data class ZReadingSummary(
    val grossSales: Double,
    val discounts: Double,
    val netRevenue: Double,
    val cashSales: Double,
    val gcashSales: Double,
    val totalExpenses: Double,
    val startingCashFloat: Double,
    val cashDrawer: Double,
    val profits: Double,
    val topSellingItem: Pair<String, Int>?,
    val orderCount: Int
)
