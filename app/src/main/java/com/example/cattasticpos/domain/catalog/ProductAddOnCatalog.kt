package com.example.cattasticpos.domain.catalog

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
        AddOnOption("spicy", "Spicy", ADD_ON_PRICE)
    )

    private val buldakAddOns = listOf(
        AddOnOption("egg_sunny", "Egg (Sunny Side Up)", 15.0),
        AddOnOption("egg_omelette", "Egg (Omelette)", 15.0),
        AddOnOption("egg_boiled", "Egg (Boiled)", 15.0),
        AddOnOption("seaweed", "3pcs Seaweed", 25.0),
        AddOnOption("hotdog", "Hotdog", 20.0),
        AddOnOption("fries", "Fries", 30.0)
    )

    fun addOnsForItem(itemId: String): List<AddOnOption> = when {
        itemId == "drink_soda" -> sodaAddOns
        itemId == "bite_takoyaki" -> takoyakiAddOns
        itemId.startsWith("buldak_") || itemId.startsWith("sedaap_") -> buldakAddOns
        else -> emptyList()
    }

    fun supportsAddOns(itemId: String): Boolean = addOnsForItem(itemId).isNotEmpty()

    fun allowsMultiple(itemId: String): Boolean =
        itemId == "drink_soda" || itemId.startsWith("buldak_") || itemId.startsWith("sedaap_")

    fun surcharge(itemId: String, selectedAddOnIds: List<String>): Double {
        val options = addOnsForItem(itemId).associateBy { it.id }
        return selectedAddOnIds.sumOf { options[it]?.price ?: 0.0 }
    }

    fun labelsForIds(itemId: String, selectedAddOnIds: List<String>): List<String> {
        val options = addOnsForItem(itemId).associateBy { it.id }
        return selectedAddOnIds.mapNotNull { options[it]?.label }
    }
}
