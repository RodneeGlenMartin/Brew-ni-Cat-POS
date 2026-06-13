package com.example.cattasticpos.domain.model

import com.example.cattasticpos.domain.catalog.ProductAddOnCatalog

/**
 * Parses cart/order flavor strings that may include coffee options (" — ") and add-ons (" + ").
 */
data class CartLineSelection(
    val baseFlavor: String?,
    val coffeeOption: String?,
    val addOnLabels: List<String>
) {
    fun encode(): String? = buildString {
        if (!baseFlavor.isNullOrBlank()) append(baseFlavor)
        if (!coffeeOption.isNullOrBlank()) {
            if (isNotEmpty()) append(" — ")
            append(coffeeOption)
        }
        if (addOnLabels.isNotEmpty()) {
            if (isNotEmpty()) append(" + ")
            append(addOnLabels.joinToString(", "))
        }
    }.takeIf { it.isNotBlank() }

    fun addOnSurcharge(itemId: String): Double {
        val options = ProductAddOnCatalog.addOnsForItem(itemId).associateBy { it.label }
        return addOnLabels.sumOf { label -> options[label]?.price ?: 0.0 }
    }

    companion object {
        fun parse(flavor: String?, itemId: String): CartLineSelection {
            if (flavor.isNullOrBlank()) {
                return CartLineSelection(null, null, emptyList())
            }
            val addOnPart = flavor.substringAfter(" + ", missingDelimiterValue = "").trim()
            val beforeAddOns = flavor.substringBefore(" + ").trim()
            val coffeeOption = beforeAddOns.substringAfter(" — ", missingDelimiterValue = "").trim()
                .takeIf { beforeAddOns.contains(" — ") }
            val baseFlavor = beforeAddOns.substringBefore(" — ").trim().takeIf { it.isNotBlank() }
            val addOnLabels = if (addOnPart.isNotEmpty()) {
                addOnPart.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
            return CartLineSelection(baseFlavor, coffeeOption, addOnLabels)
        }

        fun encode(
            baseFlavor: String?,
            coffeeOption: String?,
            addOnIds: List<String>,
            itemId: String
        ): String? = CartLineSelection(
            baseFlavor = baseFlavor,
            coffeeOption = coffeeOption,
            addOnLabels = ProductAddOnCatalog.labelsForIds(itemId, addOnIds)
        ).encode()
    }
}
