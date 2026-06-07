package com.example.cattasticpos.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.example.cattasticpos.data.local.entity.OrderEntity
import com.example.cattasticpos.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)

data class TopSellingItemResult(
    val itemName: String,
    val totalQuantity: Int
)

data class CashierSalesResult(
    val cashierId: String?,
    val totalSales: Double?
)

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity)

    @Insert
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    suspend fun insertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        insertOrder(order)
        insertOrderItems(items)
    }

    @Transaction
    @Query(
        """
        SELECT * FROM orders
        WHERE timestamp >= :startDate AND timestamp <= :endDate
        AND timestamp < :beforeTimestamp
        ORDER BY timestamp DESC
        LIMIT :limit
        """
    )
    fun observeOrdersPage(startDate: Long, endDate: Long, beforeTimestamp: Long, limit: Int): Flow<List<OrderWithItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM orders
        WHERE timestamp >= :startDate AND timestamp <= :endDate
        AND timestamp < :beforeTimestamp
        ORDER BY timestamp DESC
        LIMIT :limit
        """
    )
    suspend fun getOrdersPage(startDate: Long, endDate: Long, beforeTimestamp: Long, limit: Int): List<OrderWithItems>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderWithItems(orderId: String): OrderWithItems?

    @Query("SELECT itemName, SUM(quantity) as totalQuantity FROM order_items JOIN orders ON order_items.orderId = orders.id WHERE orders.timestamp >= :startOfDay AND orders.timestamp <= :endOfDay GROUP BY itemName ORDER BY totalQuantity DESC LIMIT 1")
    fun getTopSellingItemForDay(startOfDay: Long, endOfDay: Long): Flow<TopSellingItemResult?>

    @Query("SELECT SUM(subtotal) FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getGrossSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(discountDeduction) FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getDiscountsGivenForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(total) FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getNetRevenueForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(total) FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND paymentMethod = 'CASH'")
    fun getCashSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(total) FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND paymentMethod = 'GCASH'")
    fun getGcashSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query(
        """
        SELECT cashierId, SUM(total) as totalSales FROM orders
        WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay
        GROUP BY cashierId
        """
    )
    fun observeCashierSalesForDay(startOfDay: Long, endOfDay: Long): Flow<List<CashierSalesResult>>

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderEntity(orderId: String)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItemsForOrder(orderId: String)

    @Transaction
    suspend fun deleteOrderWithItems(orderId: String) {
        deleteOrderItemsForOrder(orderId)
        deleteOrderEntity(orderId)
    }
}
