package com.example.cattasticpos.domain.model

data class CartKey(
    val itemId: String,
    val variantId: String,
    val flavor: String?
) {
    fun displayId(): String = "$itemId\u0001$variantId\u0001${flavor.orEmpty()}"

    companion object {
        fun from(item: Item, variant: Variant, flavor: String?): CartKey {
            return CartKey(itemId = item.id, variantId = variant.id, flavor = flavor)
        }
    }
}

data class CartItem(
    val key: CartKey,
    val item: Item,
    val variant: Variant,
    val flavor: String?,
    val quantity: Int
) {
    val id: String get() = key.displayId()

    val unitPrice: Double
        get() {
            val selection = CartLineSelection.parse(flavor, item.id)
            val basePrice = try {
                variant.getPrice(selection.baseFlavor)
            } catch (e: Exception) {
                variant.basePrice
            }
            return basePrice + selection.addOnSurcharge(item.id)
        }

    val totalPrice: Double
        get() = unitPrice * quantity
}
