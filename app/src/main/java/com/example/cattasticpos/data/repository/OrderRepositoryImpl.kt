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

    override suspend fun getOrderById(orderId: String): Order? {
        return orderDao.getOrderWithItems(orderId)?.toDomain()
    }

    override suspend fun saveOrder(order: Order) {
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
            tableLabel = order.tableLabel
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
            orderDao.insertOrder(orderEntity)
            orderDao.insertOrderItems(itemEntities)
        }
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

    override suspend fun deleteOrder(orderId: String) {
        database.withTransaction {
            orderDao.deleteOrderItemsForOrder(orderId)
            orderDao.deleteOrderEntity(orderId)
        }
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
            tableLabel = order.tableLabel,
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
