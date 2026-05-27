package com.example.cattasticpos.data.repository

import com.example.cattasticpos.data.local.dao.OrderDao
import com.example.cattasticpos.data.local.entity.OrderEntity
import com.example.cattasticpos.data.local.entity.OrderItemEntity
import com.example.cattasticpos.domain.model.Order
import com.example.cattasticpos.domain.model.OrderItem
import com.example.cattasticpos.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepositoryImpl(
    private val orderDao: OrderDao
) : OrderRepository {

    override fun getOrdersWithItems(): Flow<List<Order>> {
        return orderDao.getOrdersWithItems().map { list ->
            list.map { wrapper ->
                Order(
                    id = wrapper.order.id,
                    timestamp = wrapper.order.timestamp,
                    subtotal = wrapper.order.subtotal,
                    discountDeduction = wrapper.order.discountDeduction,
                    discountLabel = wrapper.order.discountLabel,
                    total = wrapper.order.total,
                    paymentMethod = wrapper.order.paymentMethod,
                    paymentReference = wrapper.order.paymentReference,
                    items = wrapper.items.map { item ->
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
            paymentReference = order.paymentReference
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
        orderDao.insertOrderWithItems(orderEntity, itemEntities)
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

    override suspend fun deleteOrder(orderId: String) {
        orderDao.deleteOrderWithItems(orderId)
    }
}
