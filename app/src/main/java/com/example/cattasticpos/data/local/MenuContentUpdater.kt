package com.example.cattasticpos.data.local

import com.example.cattasticpos.data.local.dao.InventoryDao
import com.example.cattasticpos.data.local.dao.MenuDao
import com.example.cattasticpos.data.local.dao.RecipeDao
import com.example.cattasticpos.data.local.entity.InventoryEntity
import com.example.cattasticpos.data.local.entity.ItemEntity
import com.example.cattasticpos.data.local.entity.RecipeMappingEntity

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
              {"id":"4pcs","name":"4pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":40.0,"Cheesy Calico":45.0,"Octo-Paws":55.0,"Shrimp":55.0}},
              {"id":"8pcs","name":"8pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":80.0,"Cheesy Calico":85.0,"Octo-Paws":110.0,"Shrimp":110.0}},
              {"id":"12pcs","name":"12pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":120.0,"Cheesy Calico":130.0,"Octo-Paws":160.0,"Shrimp":160.0}},
              {"id":"16pcs","name":"16pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":150.0,"Cheesy Calico":170.0,"Octo-Paws":210.0,"Shrimp":210.0}}
            ]
        """.trimIndent()
    )

    fun shrimpTakoyakiRecipeMappings(): List<RecipeMappingEntity> = listOf(
        RecipeMappingEntity("r_shrimp_4", "bite_takoyaki", "4pcs|Shrimp", "inv_shrimp", 4.0),
        RecipeMappingEntity("r_shrimp_8", "bite_takoyaki", "8pcs|Shrimp", "inv_shrimp", 8.0),
        RecipeMappingEntity("r_shrimp_12", "bite_takoyaki", "12pcs|Shrimp", "inv_shrimp", 12.0),
        RecipeMappingEntity("r_shrimp_16", "bite_takoyaki", "16pcs|Shrimp", "inv_shrimp", 16.0)
    )
}
