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
                sizeVariantName = cartItem.variant.name,
                flavor = cartItem.flavor,
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
                sizeVariantName = orderItem.variantName,
                flavor = orderItem.flavor,
                quantity = orderItem.quantity,
                recipeRepository = recipeRepository,
                inventoryRepository = inventoryRepository
            )
        }
    }

    private suspend fun restoreComponents(
        menuItemId: String,
        variantId: String,
        sizeVariantName: String,
        flavor: String?,
        quantity: Int,
        recipeRepository: RecipeRepository,
        inventoryRepository: InventoryRepository
    ) {
        val components = ComboBundleResolver.expand(
            menuItemId = menuItemId,
            variantId = variantId,
            sizeVariantName = sizeVariantName,
            flavor = flavor,
            orderQuantity = quantity
        )
        components.forEach { component ->
            val mappings = recipeRepository.resolveCheckoutMappings(
                component.menuItemId,
                component.sizeVariantName,
                component.flavor
            )
            mappings.forEach { mapping ->
                val restoreAmount = mapping.deductionQuantity * component.quantity
                if (restoreAmount > 0) {
                    inventoryRepository.restockItem(mapping.inventoryItemId, restoreAmount)
                }
            }
        }
    }
}
