package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.OrderItem
import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.domain.repository.RecipeRepository

object InventoryRestorationHelper {
    suspend fun restoreForCartItems(
        items: List<CartItem>,
        recipeRepository: RecipeRepository,
        inventoryRepository: InventoryRepository
    ) {
        items.forEach { cartItem ->
            restoreComponents(
                menuItemId = cartItem.item.id,
                variantId = cartItem.variant.id,
                quantity = cartItem.quantity,
                recipeRepository = recipeRepository,
                inventoryRepository = inventoryRepository
            )
        }
    }

    suspend fun restoreForOrderItems(
        items: List<OrderItem>,
        recipeRepository: RecipeRepository,
        inventoryRepository: InventoryRepository
    ) {
        items.forEach { orderItem ->
            restoreComponents(
                menuItemId = orderItem.itemId,
                variantId = orderItem.variantId,
                quantity = orderItem.quantity,
                recipeRepository = recipeRepository,
                inventoryRepository = inventoryRepository
            )
        }
    }

    private suspend fun restoreComponents(
        menuItemId: String,
        variantId: String,
        quantity: Int,
        recipeRepository: RecipeRepository,
        inventoryRepository: InventoryRepository
    ) {
        val components = ComboBundleResolver.expand(menuItemId, variantId, quantity)
        components.forEach { component ->
            val mappings = recipeRepository.getMappingsForCheckout(component.menuItemId, component.variantName)
            mappings.forEach { mapping ->
                val restoreAmount = mapping.deductionQuantity * component.quantity
                if (restoreAmount > 0) {
                    inventoryRepository.restockItem(mapping.inventoryItemId, restoreAmount)
                }
            }
        }
    }
}
