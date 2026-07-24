package com.example.cattasticpos.domain.catalog

import com.example.cattasticpos.domain.model.Item

data class AddOnOption(
    val id: String,
    val label: String,
    val price: Double
)

object ProductAddOnCatalog {
    private const val ADD_ON_PRICE = 10.0

    private val sodaAddOns = listOf(
        AddOnOption("nata", "Nata de coco", ADD_ON_PRICE),
        AddOnOption("rainbow", "Rainbow Jelly", ADD_ON_PRICE)
    )

    private val takoyakiAddOns = listOf(
        AddOnOption("takeout_box", "Take-out Box", ADD_ON_PRICE)
    )

    private val buldakAddOns = listOf(
        AddOnOption("egg_sunny", "Egg (Sunny Side Up)", 15.0),
        AddOnOption("egg_omelette", "Egg (Omelette)", 15.0),
        AddOnOption("egg_boiled", "Egg (Boiled)", 15.0),
        AddOnOption("seaweed", "3pcs Seaweed", 25.0),
        AddOnOption("hotdog", "Hotdog", 20.0),
        AddOnOption("fries", "Fries", 30.0)
    )

    fun addOnsForItem(itemId: String, categoryId: String? = null, itemName: String? = null): List<AddOnOption> {
        val id = itemId.lowercase()
        val cat = categoryId?.lowercase().orEmpty()
        val name = itemName?.lowercase().orEmpty()

        return when {
            id == "drink_soda" || id.contains("soda") || name.contains("soda") -> sodaAddOns
            id == "bite_takoyaki" || id.contains("takoyaki") || name.contains("takoyaki") -> takoyakiAddOns
            id.startsWith("buldak") || id.startsWith("sedaap") ||
                id.contains("buldak") || id.contains("sedaap") ||
                cat == "cat_buldak" || cat.contains("buldak") || cat.contains("sedaap") ||
                name.contains("buldak") || name.contains("sedaap") || name.contains("samyang") -> buldakAddOns
            else -> emptyList()
        }
    }

    fun addOnsForItem(item: Item): List<AddOnOption> =
        addOnsForItem(item.id, item.categoryId, item.name)

    fun supportsAddOns(itemId: String, categoryId: String? = null, itemName: String? = null): Boolean =
        addOnsForItem(itemId, categoryId, itemName).isNotEmpty()

    fun supportsAddOns(item: Item): Boolean =
        addOnsForItem(item).isNotEmpty()

    fun allowsMultiple(itemId: String, categoryId: String? = null, itemName: String? = null): Boolean {
        val id = itemId.lowercase()
        val cat = categoryId?.lowercase().orEmpty()
        val name = itemName?.lowercase().orEmpty()
        return id == "drink_soda" || id.contains("soda") || name.contains("soda") ||
            id.startsWith("buldak") || id.startsWith("sedaap") ||
            id.contains("buldak") || id.contains("sedaap") ||
            cat == "cat_buldak" || cat.contains("buldak") || cat.contains("sedaap") ||
            name.contains("buldak") || name.contains("sedaap") || name.contains("samyang")
    }

    fun allowsMultiple(item: Item): Boolean =
        allowsMultiple(item.id, item.categoryId, item.name)

    fun surcharge(itemId: String, selectedAddOnIds: List<String>, categoryId: String? = null, itemName: String? = null): Double {
        val options = addOnsForItem(itemId, categoryId, itemName).associateBy { it.id }
        return selectedAddOnIds.sumOf { options[it]?.price ?: 0.0 }
    }

    fun surcharge(item: Item, selectedAddOnIds: List<String>): Double =
        surcharge(item.id, selectedAddOnIds, item.categoryId, item.name)

    fun labelsForIds(itemId: String, selectedAddOnIds: List<String>, categoryId: String? = null, itemName: String? = null): List<String> {
        val options = addOnsForItem(itemId, categoryId, itemName).associateBy { it.id }
        return selectedAddOnIds.mapNotNull { options[it]?.label }
    }

    fun labelsForIds(item: Item, selectedAddOnIds: List<String>): List<String> =
        labelsForIds(item.id, selectedAddOnIds, item.categoryId, item.name)
}

