package com.example.cattasticpos.data.repository

import androidx.room.withTransaction
import com.example.cattasticpos.data.local.PosDatabase
import com.example.cattasticpos.data.local.dao.OrderDao
import com.example.cattasticpos.data.local.dao.OrderWithItems
import com.example.cattasticpos.data.local.entity.OrderEntity
import com.example.cattasticpos.data.local.entity.OrderItemEntity
import com.example.cattasticpos.domain.model.Order
import com.example.cattasticpos.domain.model.OrderItem
import com.example.cattasticpos.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OrderRepositoryImpl(
    private val database: PosDatabase
) : OrderRepository {

    private val orderDao: OrderDao = database.orderDao()

    override fun observeOrdersPage(
        startDate: Long,
        endDate: Long,
        beforeTimestamp: Long,
        limit: Int
    ): Flow<List<Order>> {
        return orderDao.observeOrdersPage(startDate, endDate, beforeTimestamp, limit)
            .map { page -> page.map { it.toDomain() } }
    }

    override suspend fun getOrdersPage(
        startDate: Long,
        endDate: Long,
        beforeTimestamp: Long,
        limit: Int
    ): List<Order> {
        return orderDao.getOrdersPage(startDate, endDate, beforeTimestamp, limit).map { it.toDomain() }
    }

    override suspend fun getOrderById(orderId: Long): Order? {
        return orderDao.getOrderWithItems(orderId)?.toDomain()
    }

    override suspend fun updateOrder(order: Order): Order {
        val existing = orderDao.getOrderWithItems(order.id)
        val targetDeviceId = existing?.order?.deviceId ?: order.deviceId.takeIf { it.isNotEmpty() } ?: ""
        val orderEntity = OrderEntity(
            id = order.id,
            timestamp = order.timestamp,
            subtotal = order.subtotal,
            discountDeduction = order.discountDeduction,
            discountLabel = order.discountLabel,
            total = order.total,
            paymentMethod = order.paymentMethod,
            paymentReference = order.paymentReference,
            cashierId = order.cashierId,
            cashierName = order.cashierName,
            tableLabel = order.tableLabel,
            isServed = order.isServed,
            deviceId = targetDeviceId,
            syncStatus = "PENDING"
        )
        val itemEntities = order.items.map { item ->
            OrderItemEntity(
                orderId = order.id,
                itemId = item.itemId,
                itemName = item.itemName,
                variantId = item.variantId,
                variantName = item.variantName,
                flavor = item.flavor,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                totalPrice = item.totalPrice
            )
        }
        database.withTransaction {
            orderDao.replaceOrderWithItems(orderEntity, itemEntities)
        }
        return order
    }

    override suspend fun saveOrder(order: Order): Order {
        val appConfig = database.appConfigDao().getAppConfigOnce()
        val localDeviceId = appConfig?.deviceId.orEmpty()
        val orderEntity = OrderEntity(
            id = 0,
            timestamp = order.timestamp,
            subtotal = order.subtotal,
            discountDeduction = order.discountDeduction,
            discountLabel = order.discountLabel,
            total = order.total,
            paymentMethod = order.paymentMethod,
            paymentReference = order.paymentReference,
            cashierId = order.cashierId,
            cashierName = order.cashierName,
            tableLabel = order.tableLabel,
            isServed = order.isServed,
            deviceId = localDeviceId,
            syncStatus = "PENDING"
        )
        val itemEntities = order.items.map { item ->
            OrderItemEntity(
                orderId = 0,
                itemId = item.itemId,
                itemName = item.itemName,
                variantId = item.variantId,
                variantName = item.variantName,
                flavor = item.flavor,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                totalPrice = item.totalPrice
            )
        }
        val newOrderId = database.withTransaction {
            orderDao.insertOrderWithItems(orderEntity, itemEntities)
        }
        return order.copy(
            id = newOrderId,
            items = order.items.map { it.copy(orderId = newOrderId) }
        )
    }

    override fun getTopSellingItemForDay(startOfDay: Long, endOfDay: Long): Flow<Pair<String, Int>?> {
        return orderDao.getTopSellingItemForDay(startOfDay, endOfDay).map { result ->
            result?.let { Pair(it.itemName, it.totalQuantity) }
        }
    }

    override fun getGrossSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?> {
        return orderDao.getGrossSalesForDay(startOfDay, endOfDay)
    }

    override fun getDiscountsGivenForDay(startOfDay: Long, endOfDay: Long): Flow<Double?> {
        return orderDao.getDiscountsGivenForDay(startOfDay, endOfDay)
    }

    override fun getNetRevenueForDay(startOfDay: Long, endOfDay: Long): Flow<Double?> {
        return orderDao.getNetRevenueForDay(startOfDay, endOfDay)
    }

    override fun getCashSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?> {
        return orderDao.getCashSalesForDay(startOfDay, endOfDay)
    }

    override fun getGcashSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?> {
        return orderDao.getGcashSalesForDay(startOfDay, endOfDay)
    }

    override fun observeCashierSalesForDay(startOfDay: Long, endOfDay: Long): Flow<Map<String, Double>> {
        return orderDao.observeCashierSalesForDay(startOfDay, endOfDay).map { rows ->
            rows.mapNotNull { row ->
                val id = row.cashierId ?: return@mapNotNull null
                id to (row.totalSales ?: 0.0)
            }.toMap()
        }
    }

    override suspend fun deleteOrder(orderId: Long) {
        database.withTransaction {
            orderDao.deleteOrderItemsForOrder(orderId)
            orderDao.deleteOrderEntity(orderId)
        }

        // Asynchronously delete from Supabase if configured
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val config = database.appConfigDao().getAppConfigOnce()
                if (config != null) {
                    val supabaseUrl = config.supabaseUrl.trim()
                    val supabaseKey = config.supabaseAnonKey.trim()
                    val deviceId = config.deviceId.trim()

                    if (supabaseUrl.isNotEmpty() && supabaseKey.isNotEmpty() && deviceId.isNotEmpty()) {
                        val deviceHash = kotlin.math.abs(deviceId.hashCode()) % 1_000_000L
                        val globalOrderId = (deviceHash * 1_000_000_000L) + orderId

                        val client = okhttp3.OkHttpClient.Builder()
                            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                            .build()

                        // Delete order items
                        val deleteItemsReq = okhttp3.Request.Builder()
                            .url("$supabaseUrl/rest/v1/order_items?order_id=eq.$globalOrderId")
                            .delete()
                            .header("apikey", supabaseKey)
                            .header("Authorization", "Bearer $supabaseKey")
                            .build()
                        client.newCall(deleteItemsReq).execute().close()

                        // Delete order
                        val deleteOrderReq = okhttp3.Request.Builder()
                            .url("$supabaseUrl/rest/v1/orders?id=eq.$globalOrderId")
                            .delete()
                            .header("apikey", supabaseKey)
                            .header("Authorization", "Bearer $supabaseKey")
                            .build()
                        client.newCall(deleteOrderReq).execute().close()

                        android.util.Log.i("OrderRepositoryImpl", "Successfully deleted order $globalOrderId from Supabase.")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("OrderRepositoryImpl", "Failed to delete order $orderId from Supabase", e)
            }
        }
    }

    override suspend fun setOrderServed(orderId: Long, isServed: Boolean) {
        orderDao.setOrderServed(orderId, isServed)
    }

    private fun OrderWithItems.toDomain(): Order {
        return Order(
            id = order.id,
            timestamp = order.timestamp,
            subtotal = order.subtotal,
            discountDeduction = order.discountDeduction,
            discountLabel = order.discountLabel,
            total = order.total,
            paymentMethod = order.paymentMethod,
            paymentReference = order.paymentReference,
            cashierId = order.cashierId,
            cashierName = order.cashierName,
            tableLabel = order.tableLabel,
            isServed = order.isServed,
            deviceId = order.deviceId,
            syncStatus = order.syncStatus,
            items = items.map { item ->
                OrderItem(
                    id = item.id,
                    orderId = item.orderId,
                    itemId = item.itemId,
                    itemName = item.itemName,
                    variantId = item.variantId,
                    variantName = item.variantName,
                    flavor = item.flavor,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice
                )
            }
        )
    }
}
