package com.example.cattasticpos.domain.repository

import com.example.cattasticpos.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrdersWithItems(): Flow<List<Order>>
    suspend fun saveOrder(order: Order)
    fun getTopSellingItemForDay(startOfDay: Long, endOfDay: Long): Flow<Pair<String, Int>?>
    fun getGrossSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>
    fun getDiscountsGivenForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>
    fun getNetRevenueForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>
    fun getCashSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>
    fun getGcashSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>
    suspend fun deleteOrder(orderId: String)
}
