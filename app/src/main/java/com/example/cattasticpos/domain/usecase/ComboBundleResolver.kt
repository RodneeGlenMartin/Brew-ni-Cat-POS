package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.CartItem

/**
 * Expands combo_meals variants into component menu items for inventory deduction.
 */
object ComboBundleResolver {
    data class Component(
        val menuItemId: String,
        val sizeVariantName: String?,
        val flavor: String?,
        val quantity: Int
    )

    private val comboExpansions: Map<String, List<Component>> = mapOf(
        "combo_1" to listOf(
            Component("bite_takoyaki", "4pcs", null, 1),
            Component("drink_coffee", "16oz", null, 1)
        ),
        "combo_2" to listOf(
            Component("bite_fries", "medium", null, 1),
            Component("drink_soda", "12oz", null, 1)
        ),
        "combo_3" to listOf(
            Component("bite_nachos", "nachos_meat", null, 1),
            Component("drink_coffee", "16oz", null, 1)
        ),
        "combo_4" to listOf(
            Component("bite_takoyaki", "8pcs", null, 1),
            Component("drink_coffee", "16oz", null, 2)
        ),
        "combo_5" to listOf(
            Component("bite_nachos", "triple_purr", null, 1),
            Component("drink_coffee", "16oz", null, 2)
        ),
        "combo_6" to listOf(
            Component("bite_takoyaki", "16pcs", null, 1),
            Component("bite_fries", "barkada_overload", null, 1),
            Component("drink_soda", "16oz", null, 4)
        ),
        "combo_7" to listOf(
            Component("bite_takoyaki", "12pcs", null, 1),
            Component("bite_fries", "barkada_overload", null, 1),
            Component("bite_nachos", "meaty_meow", null, 1),
            Component("drink_coffee", "22oz", null, 4)
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
        if (menuItemId != "combo_meals") {
            return listOf(Component(menuItemId, sizeVariantName, flavor, orderQuantity))
        }
        val components = comboExpansions[variantId] ?: return emptyList()
        return components.map { it.copy(quantity = it.quantity * orderQuantity) }
    }
}
