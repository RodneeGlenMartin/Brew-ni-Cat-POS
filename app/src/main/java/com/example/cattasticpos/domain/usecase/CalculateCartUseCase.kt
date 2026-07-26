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

        // Settle the discount on a whole peso before deriving the total.
        //
        // Every money surface in the app formats with "%.0f", but the raw values were stored
        // unrounded, so a percentage discount that landed on half a peso printed a receipt that
        // did not add up: a 5% discount on a PHP 90 order showed "90 - 5 = 86" while the database
        // kept 85.50. The cashier collected the displayed 86, the Z-Reading counted 85.50, and the
        // drawer drifted by 50 centavos on every such sale. Rounding here makes the stored figure
        // and the printed one the same number everywhere at once — receipt, CSV, cloud, Z-Reading.
        // Math.round is HALF_UP, matching the "%.0f" the receipt has always printed.
        // kotlin.math.round would be HALF_EVEN and would turn a 4.50 discount into 4.
        val deduction = Math.round(discountResult.deduction).toDouble().coerceIn(0.0, subtotal)
        val total = (subtotal - deduction).coerceAtLeast(0.0)
        return CartCalculationResult(
            subtotal = subtotal,
            discountDeduction = deduction,
            discountLabel = discountResult.label,
            total = total
        )
    }
}
