package com.example.cattasticpos.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
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
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    suspend fun insertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>): Long {
        val orderId = insertOrder(order)
        if (items.isNotEmpty()) {
            insertOrderItems(items.map { it.copy(orderId = orderId) })
        }
        return orderId
    }

    @Transaction
    @Query(
        """
        SELECT * FROM orders
        WHERE timestamp >= :startDate AND timestamp <= :endDate
        AND timestamp < :beforeTimestamp
        AND isVoided = 0
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
        AND isVoided = 0
        ORDER BY timestamp DESC
        LIMIT :limit
        """
    )
    suspend fun getOrdersPage(startDate: Long, endDate: Long, beforeTimestamp: Long, limit: Int): List<OrderWithItems>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderWithItems(orderId: Long): OrderWithItems?

    @Transaction
    @Query("SELECT * FROM orders WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getOrderByRemoteId(remoteId: Long): OrderWithItems?

    @Query("UPDATE orders SET remoteId = :remoteId WHERE id = :id")
    suspend fun setRemoteId(id: Long, remoteId: Long)

    /**
     * Orders awaiting upload. Unlike [getOrdersPage] this intentionally does NOT filter out
     * voided rows, so a locally-voided order still propagates its void to the cloud.
     */
    @Transaction
    @Query("SELECT * FROM orders WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncOrders(): List<OrderWithItems>

    @Query("SELECT itemName, SUM(quantity) as totalQuantity FROM order_items JOIN orders ON order_items.orderId = orders.id WHERE orders.timestamp >= :startOfDay AND orders.timestamp <= :endOfDay AND orders.isVoided = 0 GROUP BY itemName ORDER BY totalQuantity DESC LIMIT 1")
    fun getTopSellingItemForDay(startOfDay: Long, endOfDay: Long): Flow<TopSellingItemResult?>

    @Query("SELECT SUM(subtotal) FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND isVoided = 0")
    fun getGrossSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(discountDeduction) FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND isVoided = 0")
    fun getDiscountsGivenForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(total) FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND isVoided = 0")
    fun getNetRevenueForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(total) FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND paymentMethod = 'CASH' AND isVoided = 0")
    fun getCashSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(total) FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND paymentMethod = 'GCASH' AND isVoided = 0")
    fun getGcashSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query(
        """
        SELECT cashierId, SUM(total) as totalSales FROM orders
        WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND isVoided = 0
        GROUP BY cashierId
        """
    )
    fun observeCashierSalesForDay(startOfDay: Long, endOfDay: Long): Flow<List<CashierSalesResult>>

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderEntity(orderId: Long)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItemsForOrder(orderId: Long)

    @Transaction
    suspend fun deleteOrderWithItems(orderId: Long) {
        deleteOrderItemsForOrder(orderId)
        deleteOrderEntity(orderId)
    }

    @Query("UPDATE orders SET isServed = :isServed WHERE id = :orderId")
    suspend fun setOrderServed(orderId: Long, isServed: Boolean)

    @Update
    suspend fun updateOrderEntity(order: OrderEntity)

    @Transaction
    suspend fun replaceOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        updateOrderEntity(order)
        deleteOrderItemsForOrder(order.id)
        if (items.isNotEmpty()) {
            insertOrderItems(items.map { it.copy(orderId = order.id) })
        }
    }
}
