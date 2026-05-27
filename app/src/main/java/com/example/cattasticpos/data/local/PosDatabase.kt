package com.example.cattasticpos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cattasticpos.data.local.dao.MenuDao
import com.example.cattasticpos.data.local.dao.OrderDao
import com.example.cattasticpos.data.local.entity.CategoryEntity
import com.example.cattasticpos.data.local.entity.ItemEntity
import com.example.cattasticpos.data.local.entity.OrderEntity
import com.example.cattasticpos.data.local.entity.OrderItemEntity
import com.example.cattasticpos.data.local.entity.ExpenseEntity
import com.example.cattasticpos.data.local.dao.ExpenseDao
import com.example.cattasticpos.data.local.entity.InventoryEntity
import com.example.cattasticpos.data.local.dao.InventoryDao
import com.example.cattasticpos.data.local.entity.RecipeMappingEntity
import com.example.cattasticpos.data.local.dao.RecipeDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        ItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        ExpenseEntity::class,
        InventoryEntity::class,
        RecipeMappingEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun orderDao(): OrderDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: PosDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PosDatabase::class.java,
                    "pos_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(PosDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PosDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    prepopulateDatabase(database.menuDao(), database.inventoryDao(), database.recipeDao())
                }
            }
        }

        private suspend fun prepopulateDatabase(menuDao: MenuDao, inventoryDao: InventoryDao, recipeDao: RecipeDao) {
            val categories = listOf(
                CategoryEntity("cat_bites", "Cat-Tastic Bites"),
                CategoryEntity("cat_drinks", "Cat-Tastic Drinks")
            )
            menuDao.insertCategories(categories)

            val items = listOf(
                ItemEntity(
                    id = "bite_takoyaki",
                    categoryId = "cat_bites",
                    name = "Takoyaki (Pawsome Octopus Balls)",
                    flavors = "Veggie Whiskers|Cheesy Calico|Octo-Paws",
                    variantsJson = """
                        [
                          {"id":"4pcs","name":"4pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":40.0,"Cheesy Calico":45.0,"Octo-Paws":55.0}},
                          {"id":"8pcs","name":"8pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":80.0,"Cheesy Calico":85.0,"Octo-Paws":110.0}},
                          {"id":"12pcs","name":"12pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":120.0,"Cheesy Calico":130.0,"Octo-Paws":160.0}},
                          {"id":"16pcs","name":"16pcs","basePrice":0.0,"priceByFlavor":{"Veggie Whiskers":150.0,"Cheesy Calico":170.0,"Octo-Paws":210.0}}
                        ]
                    """.trimIndent()
                ),
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
                    flavors = "Classic: Americano|Classic: Latte|Classic: Cappuccino|Classic: Espresso|Oreo: Oreo Latte|Oreo: Oreo Frappe|Oreo: Oreo Cappuccino|Matcha: Matcha Latte|Matcha: Matcha Frappe|Matcha: Matcha Espresso|Sweet Filipino: Kapeng Barako|Sweet Filipino: Cafe de Yema|Sweet Filipino: Dirty Horchata",
                    variantsJson = """
                        [
                          {"id":"12oz","name":"12oz","basePrice":49.0,"priceByFlavor":{}},
                          {"id":"16oz","name":"16oz","basePrice":59.0,"priceByFlavor":{}},
                          {"id":"22oz","name":"22oz","basePrice":79.0,"priceByFlavor":{}}
                        ]
                    """.trimIndent()
                )
            )
            menuDao.insertItems(items)

            val inventoryItems = listOf(
                InventoryEntity("inv_cups", "Cups", "pcs", 100, 20),
                InventoryEntity("inv_takoyaki", "Takoyaki Balls", "pcs", 100, 20),
                InventoryEntity("inv_fries", "Potato Fries", "grams", 100, 10),
                InventoryEntity("inv_nachos", "Nacho Chips", "grams", 100, 10)
            )
            inventoryDao.insertInventoryItems(inventoryItems)

            val recipeMappings = listOf(
                RecipeMappingEntity("r_tako_4", "bite_takoyaki", "4pcs", "inv_takoyaki", 4.0),
                RecipeMappingEntity("r_tako_8", "bite_takoyaki", "8pcs", "inv_takoyaki", 8.0),
                RecipeMappingEntity("r_tako_12", "bite_takoyaki", "12pcs", "inv_takoyaki", 12.0),
                RecipeMappingEntity("r_tako_16", "bite_takoyaki", "16pcs", "inv_takoyaki", 16.0),
                
                RecipeMappingEntity("r_fries_all", "bite_fries", null, "inv_fries", 150.0),
                RecipeMappingEntity("r_nachos_all", "bite_nachos", null, "inv_nachos", 150.0),
                
                RecipeMappingEntity("r_soda_all", "drink_soda", null, "inv_cups", 1.0),
                RecipeMappingEntity("r_coffee_all", "drink_coffee", null, "inv_cups", 1.0)
            )
            recipeDao.insertMappings(recipeMappings)
        }
    }
}
