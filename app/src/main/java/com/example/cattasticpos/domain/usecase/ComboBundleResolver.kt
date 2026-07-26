package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.CartItem

/**
 * Expands combo variants into component menu items for inventory deduction.
 */
object ComboBundleResolver {
    /**
     * @param sizeVariantName must be the variant's **name** as it appears in the menu catalog
     *   (e.g. "Small", "Nachos+Veggies+Meat"), not its id — [RecipeDeductionResolver] matches it
     *   against `RecipeMapping.variantName`. Several entries below used the id, which happens to
     *   be harmless today only because fries, nachos, soda and coffee all deduct through a base
     *   (null-target) mapping that matches regardless. The moment anyone adds a size-specific
     *   recipe row, an id here would silently stop deducting. CatalogConsistencyTest pins this.
     */
    data class Component(
        val menuItemId: String,
        val sizeVariantName: String?,
        val flavor: String?,
        val quantity: Int
    )

    private const val COFFEE_COMPONENT_ID = "drink_cat_feine"

    private val comboMenuItemIds = setOf(
        "combo_meals",
        "combo_single_paw",
        "combo_couple_cats",
        "combo_association"
    )

    private val comboExpansions: Map<String, List<Component>> = mapOf(
        "combo_1" to listOf(
            Component("bite_takoyaki", "4pcs", null, 1),
            Component(COFFEE_COMPONENT_ID, "16oz", null, 1)
        ),
        "combo_2" to listOf(
            Component("bite_fries", "Small", null, 1),
            Component("drink_soda", "12oz", null, 1)
        ),
        "combo_3" to listOf(
            Component("bite_nachos", "Nachos+Veggies+Meat", null, 1),
            Component(COFFEE_COMPONENT_ID, "16oz", null, 1)
        ),
        "combo_4" to listOf(
            Component("bite_takoyaki", "4pcs", null, 1),
            Component("drink_soda", "12oz", null, 1)
        ),
        "combo_5" to listOf(
            Component("bite_takoyaki", "8pcs", null, 1),
            Component("drink_soda", "16oz", null, 2)
        ),
        "combo_6" to listOf(
            Component("bite_fries", "Medium", null, 1),
            Component(COFFEE_COMPONENT_ID, "12oz", null, 2)
        ),
        "combo_7" to listOf(
            Component("bite_nachos", "Nachos+Veggies+Meat", null, 1),
            Component("drink_soda", "12oz", null, 2)
        ),
        "combo_8" to listOf(
            Component("bite_takoyaki", "8pcs", null, 1),
            Component("drink_soda", "16oz", null, 1),
            Component(COFFEE_COMPONENT_ID, "16oz", null, 1)
        ),
        "combo_9" to listOf(
            Component("bite_takoyaki", "8pcs", null, 1),
            Component(COFFEE_COMPONENT_ID, "16oz", null, 2)
        ),
        "combo_10" to listOf(
            Component("bite_takoyaki", "16pcs", null, 1),
            Component("bite_fries", "Jumbo", null, 1),
            Component("drink_soda", "12oz", null, 4)
        ),
        "combo_11" to listOf(
            Component("bite_takoyaki", "12pcs", null, 1),
            Component("drink_soda", "16oz", null, 2),
            Component(COFFEE_COMPONENT_ID, "16oz", null, 2)
        ),
        "combo_12" to listOf(
            Component("bite_fries", "Jumbo", null, 1),
            Component("bite_nachos", "Nachos+Fries+Meat+Veggies", null, 1),
            Component(COFFEE_COMPONENT_ID, "16oz", null, 4)
        ),
        "combo_13" to listOf(
            Component("bite_fries", "Jumbo", null, 1),
            Component(COFFEE_COMPONENT_ID, "16oz", null, 3),
            Component("drink_soda", "16oz", null, 3)
        ),
        "combo_14" to listOf(
            Component("bite_fries", "Jumbo", null, 1),
            Component("bite_takoyaki", "16pcs", null, 1),
            Component("drink_soda", "22oz", null, 2),
            Component(COFFEE_COMPONENT_ID, "22oz", null, 2)
        )
    )

    fun expandFromCartItem(cartItem: CartItem): List<Component> =
        expand(
            menuItemId = cartItem.item.id,
            variantId = cartItem.variant.id,
            sizeVariantName = cartItem.variant.name,
            flavor = cartItem.flavor,
            orderQuantity = cartItem.quantity
        )

    fun expand(
        menuItemId: String,
        variantId: String,
        sizeVariantName: String,
        flavor: String?,
        orderQuantity: Int
    ): List<Component> {
        if (menuItemId !in comboMenuItemIds) {
            return listOf(Component(menuItemId, sizeVariantName, flavor, orderQuantity))
        }
        val components = comboExpansions[variantId] ?: return emptyList()
        return components.map { it.copy(quantity = it.quantity * orderQuantity) }
    }
}
