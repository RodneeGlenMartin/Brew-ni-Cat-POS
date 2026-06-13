package com.example.cattasticpos.data.local

import com.example.cattasticpos.data.local.dao.InventoryDao
import com.example.cattasticpos.data.local.dao.MenuDao
import com.example.cattasticpos.data.local.dao.RecipeDao
import com.example.cattasticpos.data.local.entity.InventoryEntity
import com.example.cattasticpos.data.local.entity.ItemEntity
import org.json.JSONArray

/**
 * Idempotent menu/inventory patches for installs that already have seeded data.
 */
internal object MenuContentUpdater {

    suspend fun applyPendingUpdates(
        menuDao: MenuDao,
        inventoryDao: InventoryDao,
        recipeDao: RecipeDao
    ) {
        ensureShrimpInfrastructure(inventoryDao, recipeDao)
        ensureAddOnInfrastructure(inventoryDao, recipeDao)
        applyMenuBoard2026Patch(menuDao, recipeDao)
    }

    private suspend fun ensureAddOnInfrastructure(
        inventoryDao: InventoryDao,
        recipeDao: RecipeDao
    ) {
        if (inventoryDao.getInventoryCount() == 0) return
        val addOnItems = listOf(
            InventoryEntity("inv_nata_coco", "Nata de coco", "pcs", 100.0, 20.0),
            InventoryEntity("inv_rainbow_jelly", "Rainbow Jelly", "pcs", 100.0, 20.0)
        )
        val missing = addOnItems.filter { inventoryDao.getInventoryItemById(it.id) == null }
        if (missing.isNotEmpty()) {
            inventoryDao.insertInventoryItems(missing)
        }
        recipeDao.insertMappings(
            listOf(
                com.example.cattasticpos.data.local.entity.RecipeMappingEntity(
                    "r_soda_nata", "drink_soda", "Nata de coco", "inv_nata_coco", 1.0
                ),
                com.example.cattasticpos.data.local.entity.RecipeMappingEntity(
                    "r_soda_rainbow", "drink_soda", "Rainbow Jelly", "inv_rainbow_jelly", 1.0
                )
            )
        )
    }

    private suspend fun ensureShrimpInfrastructure(
        inventoryDao: InventoryDao,
        recipeDao: RecipeDao
    ) {
        if (inventoryDao.getInventoryCount() == 0) return
        val hasShrimp = inventoryDao.getInventoryItemById("inv_shrimp") != null
        if (!hasShrimp) {
            inventoryDao.insertInventoryItems(
                listOf(
                    InventoryEntity(
                        id = "inv_shrimp",
                        itemName = "Shrimp Takoyaki",
                        unit = "pcs",
                        currentStock = 100.0,
                        reorderThreshold = 20.0
                    )
                )
            )
        }
        recipeDao.insertMappings(MenuBoardCatalog.shrimpTakoyakiRecipeMappings())
    }

    private suspend fun applyMenuBoard2026Patch(menuDao: MenuDao, recipeDao: RecipeDao) {
        val combo = menuDao.getItemById("combo_meals") ?: return
        if (isMenuBoard2026Applied(combo)) return

        menuDao.insertItems(MenuBoardCatalog.allMenuItems())
        recipeDao.insertMappings(MenuBoardCatalog.shrimpTakoyakiRecipeMappings())
    }

    private fun isMenuBoard2026Applied(combo: ItemEntity): Boolean {
        return try {
            val variants = JSONArray(combo.variantsJson)
            for (i in 0 until variants.length()) {
                val variant = variants.getJSONObject(i)
                if (variant.getString("id") == "combo_1") {
                    return variant.getString("name") == "Classy Cat Combo" &&
                        variant.getDouble("basePrice") == 105.0
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }
}
