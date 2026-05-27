package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.strategy.DiscountStrategy

data class CartCalculationResult(
    val subtotal: Double,
    val discountDeduction: Double,
    val discountLabel: String,
    val total: Double
)

class CalculateCartUseCase {
    operator fun invoke(items: List<CartItem>, strategy: DiscountStrategy): CartCalculationResult {
        val subtotal = items.sumOf { it.totalPrice }
        val discountResult = strategy.applyDiscount(subtotal, items)
        val total = (subtotal - discountResult.deduction).coerceAtLeast(0.0)
        return CartCalculationResult(
            subtotal = subtotal,
            discountDeduction = discountResult.deduction,
            discountLabel = discountResult.label,
            total = total
        )
    }
}
