package com.example.cattasticpos.domain.model

data class CartItem(
    val id: String, // Composed of: itemId + "_" + variantId + "_" + (flavor ?: "")
    val item: Item,
    val variant: Variant,
    val flavor: String?,
    val quantity: Int
) {
    val unitPrice: Double
        get() = try {
            variant.getPrice(flavor)
        } catch (e: Exception) {
            variant.basePrice
        }

    val totalPrice: Double
        get() = unitPrice * quantity
}
