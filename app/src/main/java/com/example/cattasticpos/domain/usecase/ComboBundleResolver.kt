package com.example.cattasticpos.domain.usecase

/**
 * Expands combo_meals variants into component menu items for inventory deduction.
 */
object ComboBundleResolver {
    data class Component(val menuItemId: String, val variantName: String?, val quantity: Int)

    private val comboExpansions: Map<String, List<Component>> = mapOf(
        "combo_1" to listOf(
            Component("bite_takoyaki", "4pcs", 1),
            Component("drink_coffee", "16oz", 1)
        ),
        "combo_2" to listOf(
            Component("bite_fries", "medium", 1),
            Component("drink_soda", "12oz", 1)
        ),
        "combo_3" to listOf(
            Component("bite_nachos", "nachos_meat", 1),
            Component("drink_coffee", "16oz", 1)
        ),
        "combo_4" to listOf(
            Component("bite_takoyaki", "8pcs", 1),
            Component("drink_coffee", "16oz", 2)
        ),
        "combo_5" to listOf(
            Component("bite_nachos", "triple_purr", 1),
            Component("drink_coffee", "16oz", 2)
        ),
        "combo_6" to listOf(
            Component("bite_takoyaki", "16pcs", 1),
            Component("bite_fries", "barkada_overload", 1),
            Component("drink_soda", "16oz", 4)
        ),
        "combo_7" to listOf(
            Component("bite_takoyaki", "12pcs", 1),
            Component("bite_fries", "barkada_overload", 1),
            Component("bite_nachos", "meaty_meow", 1),
            Component("drink_coffee", "22oz", 4)
        )
    )

    fun expand(menuItemId: String, variantId: String, orderQuantity: Int): List<Component> {
        if (menuItemId != "combo_meals") {
            return listOf(Component(menuItemId, variantId, orderQuantity))
        }
        val components = comboExpansions[variantId] ?: return emptyList()
        return components.map { it.copy(quantity = it.quantity * orderQuantity) }
    }
}
