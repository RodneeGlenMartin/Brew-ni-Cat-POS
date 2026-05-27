package com.example.cattasticpos.domain.model

data class Order(
    val id: String,
    val timestamp: Long,
    val subtotal: Double,
    val discountDeduction: Double,
    val discountLabel: String,
    val total: Double,
    val paymentMethod: String,
    val paymentReference: String?,
    val items: List<OrderItem>
)

data class OrderItem(
    val id: Long,
    val orderId: String,
    val itemId: String,
    val itemName: String,
    val variantId: String,
    val variantName: String,
    val flavor: String?,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)
