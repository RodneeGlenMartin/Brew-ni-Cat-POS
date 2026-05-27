package com.example.cattasticpos.domain.model

data class Category(
    val id: String,
    val name: String
)

data class Variant(
    val id: String,
    val name: String,
    val basePrice: Double,
    val priceByFlavor: Map<String, Double> = emptyMap()
) {
    fun getPrice(flavor: String?): Double {
        if (flavor != null) {
            val price = priceByFlavor[flavor]
            if (price != null) return price
            
            // Handle cases where flavor has sub-category prefix (e.g. "Classic: Americano" matching "Americano")
            val cleanFlavor = flavor.substringAfter(": ").trim()
            val cleanPrice = priceByFlavor[cleanFlavor]
            if (cleanPrice != null) return cleanPrice
        }
        return basePrice
    }
}

data class Item(
    val id: String,
    val categoryId: String,
    val name: String,
    val flavors: List<String>,
    val variants: List<Variant>
) {
    // Helper to get starting price
    val startingPrice: Double
        get() {
            if (variants.isEmpty()) return 0.0
            return variants.minOf { variant ->
                if (variant.priceByFlavor.isNotEmpty()) {
                    variant.priceByFlavor.values.minOrNull() ?: variant.basePrice
                } else {
                    variant.basePrice
                }
            }
        }
}
