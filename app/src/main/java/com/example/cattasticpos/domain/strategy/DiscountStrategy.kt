package com.example.cattasticpos.domain.strategy

import com.example.cattasticpos.domain.model.CartItem

data class DiscountResult(val deduction: Double, val label: String)

interface DiscountStrategy {
    fun applyDiscount(subTotal: Double, items: List<CartItem>): DiscountResult
}

class NoDiscountStrategy : DiscountStrategy {
    override fun applyDiscount(subTotal: Double, items: List<CartItem>): DiscountResult {
        return DiscountResult(0.0, "None")
    }
}

class PercentageDiscountStrategy(val pct: Double) : DiscountStrategy {
    override fun applyDiscount(subTotal: Double, items: List<CartItem>): DiscountResult {
        val deduction = subTotal * (pct / 100.0)
        return DiscountResult(deduction, "${pct.toInt()}% OFF")
    }
}

class FivePercentDiscountStrategy : DiscountStrategy {
    override fun applyDiscount(subTotal: Double, items: List<CartItem>): DiscountResult {
        return DiscountResult(subTotal * 0.05, "5% OFF")
    }
}

class FreeOrderDiscountStrategy : DiscountStrategy {
    override fun applyDiscount(subTotal: Double, items: List<CartItem>): DiscountResult {
        return DiscountResult(subTotal, "100% Free Order Coupon Applied")
    }
}
