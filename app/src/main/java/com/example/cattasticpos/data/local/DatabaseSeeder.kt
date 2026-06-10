package com.example.cattasticpos.data.local

import com.example.cattasticpos.data.local.dao.AppConfigDao
import com.example.cattasticpos.data.local.dao.InventoryDao
import com.example.cattasticpos.data.local.dao.MenuDao
import com.example.cattasticpos.data.local.dao.RecipeDao
import com.example.cattasticpos.data.local.entity.AppConfigEntity
import com.example.cattasticpos.data.local.entity.CategoryEntity
import com.example.cattasticpos.data.local.entity.InventoryEntity
import com.example.cattasticpos.data.local.entity.ItemEntity
import com.example.cattasticpos.data.local.entity.RecipeMappingEntity

internal object DatabaseSeeder {

    suspend fun seedDefaultsIfMissing(
        menuDao: MenuDao,
        inventoryDao: InventoryDao,
        recipeDao: RecipeDao,
        appConfigDao: AppConfigDao
    ) {
        if (menuDao.getCategoryCount() == 0) {
            seedMenu(menuDao)
        }
        if (inventoryDao.getInventoryCount() == 0) {
            seedInventory(inventoryDao)
        }
        if (recipeDao.getMappingCount() == 0) {
            seedRecipes(recipeDao)
        }
        if (appConfigDao.getAppConfigOnce() == null) {
            seedAppConfig(appConfigDao)
        }
    }

    private suspend fun seedAppConfig(appConfigDao: AppConfigDao) {
        try {
            appConfigDao.insertConfig(
                AppConfigEntity(
                    id = 1,
                    targetSales = AppConfigEntity.DEFAULT_TARGET_SALES,
                    startingCashFloat = AppConfigEntity.DEFAULT_STARTING_CASH_FLOAT,
                    pinHash = "otCBSIxSZkk6vcF7SKwqCw==:Seyex1KVzCA7gLC3+1Vi8AHYtjU7A168GCGRihADbp0=",
                    cashiersJson = AppConfigEntity.DEFAULT_CASHIERS_JSON
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("DatabaseSeeder", "Error seeding app config", e)
        }
    }

    private suspend fun seedMenu(menuDao: MenuDao) {
        try {
            menuDao.insertCategories(
                listOf(
                    CategoryEntity("cat_bites", "Cat-Tastic Bites"),
                    CategoryEntity("cat_drinks", "Cat-Tastic Drinks"),
                    CategoryEntity("combos", "Combos & Packages")
                )
            )
            menuDao.insertItems(defaultMenuItems())
        } catch (e: Exception) {
            android.util.Log.e("DatabaseSeeder", "Error seeding menu", e)
        }
    }

    private suspend fun seedInventory(inventoryDao: InventoryDao) {
        try {
            inventoryDao.insertInventoryItems(
                listOf(
                    InventoryEntity("inv_cups", "Cups", "pcs", 100.0, 20.0),
                    InventoryEntity("inv_takoyaki", "Takoyaki Balls", "pcs", 100.0, 20.0),
                    InventoryEntity("inv_shrimp", "Shrimp Takoyaki", "pcs", 100.0, 20.0),
                    InventoryEntity("inv_fries", "Potato Fries", "grams", 100.0, 10.0),
                    InventoryEntity("inv_nachos", "Nacho Chips", "grams", 100.0, 10.0)
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("DatabaseSeeder", "Error seeding inventory", e)
        }
    }

    private suspend fun seedRecipes(recipeDao: RecipeDao) {
        try {
            recipeDao.insertMappings(
                listOf(
                    RecipeMappingEntity("r_tako_4", "bite_takoyaki", "4pcs", "inv_takoyaki", 4.0),
                    RecipeMappingEntity("r_tako_8", "bite_takoyaki", "8pcs", "inv_takoyaki", 8.0),
                    RecipeMappingEntity("r_tako_12", "bite_takoyaki", "12pcs", "inv_takoyaki", 12.0),
                    RecipeMappingEntity("r_tako_16", "bite_takoyaki", "16pcs", "inv_takoyaki", 16.0),
                    *MenuContentUpdater.shrimpTakoyakiRecipeMappings().toTypedArray(),
                    RecipeMappingEntity("r_fries_all", "bite_fries", null, "inv_fries", 150.0),
                    RecipeMappingEntity("r_nachos_all", "bite_nachos", null, "inv_nachos", 150.0),
                    RecipeMappingEntity("r_soda_all", "drink_soda", null, "inv_cups", 1.0),
                    RecipeMappingEntity("r_coffee_all", "drink_coffee", null, "inv_cups", 1.0)
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("DatabaseSeeder", "Error seeding recipe mappings", e)
        }
    }

    private fun defaultMenuItems(): List<ItemEntity> = listOf(
        MenuContentUpdater.takoyakiItemWithShrimp(),
        ItemEntity(
            id = "bite_fries",
            categoryId = "cat_bites",
            name = "Fries (Cat Claws)",
            flavors = "BBQ Scratch|Cheesy Purr|Sour & Cream Mew|Spicy Claw",
            variantsJson = """
                [
                  {"id":"small","name":"Small","basePrice":30.0,"priceByFlavor":{}},
                  {"id":"medium","name":"Medium","basePrice":50.0,"priceByFlavor":{}},
                  {"id":"large","name":"Large","basePrice":70.0,"priceByFlavor":{}},
                  {"id":"barkada_overload","name":"Barkada Overload","basePrice":150.0,"priceByFlavor":{}}
                ]
            """.trimIndent()
        ),
        ItemEntity(
            id = "bite_nachos",
            categoryId = "cat_bites",
            name = "Nachos (Kitty Litter Crisps)",
            flavors = "",
            variantsJson = """
                [
                  {"id":"nachos_veggies","name":"Nachos+Veggies","basePrice":59.0,"priceByFlavor":{}},
                  {"id":"nachos_meat","name":"Nachos+Meat","basePrice":79.0,"priceByFlavor":{}},
                  {"id":"nachos_fries","name":"Nachos+Fries","basePrice":59.0,"priceByFlavor":{}},
                  {"id":"triple_purr","name":"The Triple Purr","basePrice":99.0,"priceByFlavor":{}},
                  {"id":"garden_cat","name":"Garden Cat","basePrice":89.0,"priceByFlavor":{}},
                  {"id":"meaty_meow","name":"Meaty Meow","basePrice":99.0,"priceByFlavor":{}}
                ]
            """.trimIndent()
        ),
        ItemEntity(
            id = "drink_soda",
            categoryId = "cat_drinks",
            name = "Soda (Fizzy Felines)",
            flavors = "Yogurt Yarn|Honey Peach Paws|Passion Fruit Purr|Kiwi Kitten|Strawberry Scratch|Lychee Litter|Blueberry Bite|Grumpy Grapes|Green Apple Alley Cat",
            variantsJson = """
                [
                  {"id":"12oz","name":"12oz","basePrice":39.0,"priceByFlavor":{}},
                  {"id":"16oz","name":"16oz","basePrice":49.0,"priceByFlavor":{}},
                  {"id":"22oz","name":"22oz","basePrice":69.0,"priceByFlavor":{}}
                ]
            """.trimIndent()
        ),
        ItemEntity(
            id = "drink_coffee",
            categoryId = "cat_drinks",
            name = "Cat-Feine (Coffee)",
            flavors = "Classic: Salted Caramel Latte|Classic: Vanilla Iced Latte|Classic: Hazelnut Latte|Classic: Caramel Macchiato|Classic: Salted Caramel Hazelnut|Oreo: Caramel Oreo Coffee|Oreo: Oreo Iced Latte|Oreo: Vanilla Oreo Latte|Matcha: Dirty Matcha|Matcha: Vanilla Matcha Latte|Matcha: Caramel Matcha|Sweet Filipino: Condensed Milk Coffee|Sweet Filipino: Sea Salt Caramel Coffee",
            variantsJson = """
                [
                  {"id":"12oz","name":"12oz","basePrice":49.0,"priceByFlavor":{}},
                  {"id":"16oz","name":"16oz","basePrice":59.0,"priceByFlavor":{}},
                  {"id":"22oz","name":"22oz","basePrice":79.0,"priceByFlavor":{}}
                ]
            """.trimIndent()
        ),
        ItemEntity(
            id = "combo_meals",
            categoryId = "combos",
            name = "Combo Meals",
            flavors = "",
            variantsJson = """
                [
                  {"id":"combo_1","name":"The Classy Cat Combo","basePrice":104.0,"priceByFlavor":{},"description":"Food: 4pcs Cheesy Calico (Cheese) Takoyaki\nDrink: 16oz Salted Caramel Tail (Classic Coffee)"},
                  {"id":"combo_2","name":"The Fizzy Kitten","basePrice":89.0,"priceByFlavor":{},"description":"Food: Medium BBQ Scratch Fries\nDrink: 12oz Kiwi Kitten Soda"},
                  {"id":"combo_3","name":"The Sweet Puspin","basePrice":138.0,"priceByFlavor":{},"description":"Food: Nachos + Meat\nDrink: 16oz Condensed Milk Meow (Sweet Filipino Style Coffee)"},
                  {"id":"combo_4","name":"The Two-Tail","basePrice":228.0,"priceByFlavor":{},"description":"Food: 8pcs Octo-Paws (Octobits) Takoyaki\nDrinks: Two 16oz Oreo Coffee Drinks (e.g., Oreo Iced Paw-tte)"},
                  {"id":"combo_5","name":"Matcha Made in Heaven","basePrice":217.0,"priceByFlavor":{},"description":"Food: The Triple Purr Nachos (Nachos + Fries + Meat) to share\nDrinks: Two 16oz Matcha Coffee Drinks (e.g., Dirty Neko)"},
                  {"id":"combo_6","name":"Litter Box Feast","basePrice":496.0,"priceByFlavor":{},"description":"Food: * 16pcs Veggie Whiskers Takoyaki\n* 1 Barkada Overload Fries (Choice of flavor)\nDrinks: Four 16oz Fizzy Feline Sodas (Any flavor)"},
                  {"id":"combo_7","name":"Ultimate Alley Cat Party","basePrice":725.0,"priceByFlavor":{},"description":"Food: * 12pcs Octo-Paws Takoyaki\n* 1 Barkada Overload Fries\n* 1 Meaty Meow Nachos\nDrinks: Four 22oz Cat-Feine Coffees (Any style)"}
                ]
            """.trimIndent()
        )
    )
}
