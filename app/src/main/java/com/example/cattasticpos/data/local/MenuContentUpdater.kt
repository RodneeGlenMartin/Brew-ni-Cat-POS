package com.example.cattasticpos.data.local

import com.example.cattasticpos.data.local.dao.InventoryDao
import com.example.cattasticpos.data.local.dao.MenuDao
import com.example.cattasticpos.data.local.dao.RecipeDao
import com.example.cattasticpos.data.local.entity.InventoryEntity
import com.example.cattasticpos.data.local.entity.ItemEntity
import com.example.cattasticpos.data.local.entity.RecipeMappingEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Idempotent menu/inventory patches for installs that already have seeded data.
 */
internal object MenuContentUpdater {

    suspend fun applyPendingUpdates(
        menuDao: MenuDao,
        inventoryDao: InventoryDao,
        recipeDao: RecipeDao
    ) {
        applyShrimpTakoyaki(menuDao, inventoryDao, recipeDao)
        applyPopotPricePatch(menuDao)
    }

    private suspend fun applyShrimpTakoyaki(
        menuDao: MenuDao,
        inventoryDao: InventoryDao,
        recipeDao: RecipeDao
    ) {
        val takoyaki = menuDao.getItemById("bite_takoyaki") ?: return
        if (takoyaki.flavors.contains("Shrimp")) return

        menuDao.insertItems(listOf(takoyakiItemWithShrimp()))
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
        recipeDao.insertMappings(shrimpTakoyakiRecipeMappings())
    }

    fun takoyakiItemWithShrimp(): ItemEntity = ItemEntity(
        id = "bite_takoyaki",
        categoryId = "cat_bites",
        name = "Takoyaki (Pawsome Octopus Balls)",
        flavors = "Veggie Whiskers|Cheesy Calico|Octo-Paws|Shrimp",
        variantsJson = """
            [
              {"id":"4pcs","name":"4pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":40.0,"Cheesy Calico":40.0,"Octo-Paws":55.0,"Shrimp":55.0}},
              {"id":"8pcs","name":"8pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":80.0,"Cheesy Calico":85.0,"Octo-Paws":110.0,"Shrimp":110.0}},
              {"id":"12pcs","name":"12pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":120.0,"Cheesy Calico":130.0,"Octo-Paws":160.0,"Shrimp":160.0}},
              {"id":"16pcs","name":"16pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":150.0,"Cheesy Calico":170.0,"Octo-Paws":210.0,"Shrimp":210.0}}
            ]
        """.trimIndent()
    )

    private suspend fun applyPopotPricePatch(menuDao: MenuDao) {
        val itemsToUpdate = listOfNotNull(
            menuDao.getItemById("bite_takoyaki")?.let(::patchCheesyCalico4pcsPrice),
            menuDao.getItemById("combo_meals")?.let(::patchLitterBoxFeastPrice)
        )
        if (itemsToUpdate.isNotEmpty()) {
            menuDao.insertItems(itemsToUpdate)
        }
    }

    private fun patchCheesyCalico4pcsPrice(item: ItemEntity): ItemEntity? {
        return try {
            val variants = JSONArray(item.variantsJson)
            var changed = false
            for (i in 0 until variants.length()) {
                val variant = variants.getJSONObject(i)
                if (variant.getString("id") != "4pcs") continue
                val prices = variant.optJSONObject("priceByFlavor") ?: JSONObject()
                if (prices.optDouble("Cheesy Calico", -1.0) == 40.0) return null
                prices.put("Cheesy Calico", 40.0)
                variant.put("priceByFlavor", prices)
                changed = true
                break
            }
            if (changed) item.copy(variantsJson = variants.toString()) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun patchLitterBoxFeastPrice(item: ItemEntity): ItemEntity? {
        return try {
            val variants = JSONArray(item.variantsJson)
            var changed = false
            for (i in 0 until variants.length()) {
                val variant = variants.getJSONObject(i)
                if (variant.getString("id") != "combo_6") continue
                if (variant.getDouble("basePrice") == 300.0) return null
                variant.put("basePrice", 300.0)
                changed = true
                break
            }
            if (changed) item.copy(variantsJson = variants.toString()) else null
        } catch (_: Exception) {
            null
        }
    }

    fun shrimpTakoyakiRecipeMappings(): List<RecipeMappingEntity> = listOf(
        RecipeMappingEntity("r_shrimp_4", "bite_takoyaki", "4pcs|Shrimp", "inv_shrimp", 4.0),
        RecipeMappingEntity("r_shrimp_8", "bite_takoyaki", "8pcs|Shrimp", "inv_shrimp", 8.0),
        RecipeMappingEntity("r_shrimp_12", "bite_takoyaki", "12pcs|Shrimp", "inv_shrimp", 12.0),
        RecipeMappingEntity("r_shrimp_16", "bite_takoyaki", "16pcs|Shrimp", "inv_shrimp", 16.0)
    )
}
