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
import com.example.cattasticpos.data.local.entity.AppConfigEntity
import com.example.cattasticpos.data.local.dao.AppConfigDao
import com.example.cattasticpos.data.local.dao.VoidDao
import com.example.cattasticpos.data.local.entity.VoidRecordEntity
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
        RecipeMappingEntity::class,
        AppConfigEntity::class,
        VoidRecordEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun orderDao(): OrderDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun recipeDao(): RecipeDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun voidDao(): VoidDao

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
                .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                .addCallback(PosDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_config ADD COLUMN pinHash TEXT NOT NULL DEFAULT 'otCBSIxSZkk6vcF7SKwqCw==:Seyex1KVzCA7gLC3+1Vi8AHYtjU7A168GCGRihADbp0='")
                db.execSQL("CREATE TABLE inventory_new (id TEXT NOT NULL PRIMARY KEY, itemName TEXT NOT NULL, unit TEXT NOT NULL, currentStock REAL NOT NULL, reorderThreshold REAL NOT NULL)")
                db.execSQL("INSERT INTO inventory_new (id, itemName, unit, currentStock, reorderThreshold) SELECT id, itemName, unit, CAST(currentStock AS REAL), CAST(reorderThreshold AS REAL) FROM inventory")
                db.execSQL("DROP TABLE inventory")
                db.execSQL("ALTER TABLE inventory_new RENAME TO inventory")
            }
        }

        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS order_items")
                db.execSQL("DROP TABLE IF EXISTS void_records")
                db.execSQL("DROP TABLE IF EXISTS orders")
                db.execSQL(
                    """
                    CREATE TABLE orders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        subtotal REAL NOT NULL,
                        discountDeduction REAL NOT NULL,
                        discountLabel TEXT NOT NULL,
                        total REAL NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        paymentReference TEXT,
                        cashierId TEXT,
                        cashierName TEXT,
                        tableLabel TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE order_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        orderId INTEGER NOT NULL,
                        itemId TEXT NOT NULL,
                        itemName TEXT NOT NULL,
                        variantId TEXT NOT NULL,
                        variantName TEXT NOT NULL,
                        flavor TEXT,
                        quantity INTEGER NOT NULL,
                        unitPrice REAL NOT NULL,
                        totalPrice REAL NOT NULL,
                        FOREIGN KEY(orderId) REFERENCES orders(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_order_items_orderId ON order_items(orderId)")
                db.execSQL(
                    """
                    CREATE TABLE void_records (
                        id TEXT NOT NULL PRIMARY KEY,
                        orderId INTEGER NOT NULL,
                        reason TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        cashierId TEXT,
                        orderTotal REAL NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN cashierId TEXT")
                db.execSQL("ALTER TABLE orders ADD COLUMN tableLabel TEXT")
                db.execSQL(
                    "ALTER TABLE app_config ADD COLUMN cashiersJson TEXT NOT NULL DEFAULT '" +
                        AppConfigEntity.DEFAULT_CASHIERS_JSON.replace("'", "''") + "'"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS void_records (
                        id TEXT NOT NULL PRIMARY KEY,
                        orderId TEXT NOT NULL,
                        reason TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        cashierId TEXT,
                        orderTotal REAL NOT NULL
                    )
                    """.trimIndent()
                )
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
                    prepopulateDatabase(database.menuDao(), database.inventoryDao(), database.recipeDao(), database.appConfigDao())
                }
            }
        }

        private suspend fun prepopulateDatabase(menuDao: MenuDao, inventoryDao: InventoryDao, recipeDao: RecipeDao, appConfigDao: AppConfigDao) {
            try {
                appConfigDao.insertConfig(
                    AppConfigEntity(
                        id = 1,
                        targetSales = 5000.0,
                        startingCashFloat = 500.0,
                        pinHash = "otCBSIxSZkk6vcF7SKwqCw==:Seyex1KVzCA7gLC3+1Vi8AHYtjU7A168GCGRihADbp0=",
                        cashiersJson = AppConfigEntity.DEFAULT_CASHIERS_JSON
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("PosDatabase", "Error seeding app config", e)
            }
            
            try {
                val categories = listOf(
                    CategoryEntity("cat_bites", "Cat-Tastic Bites"),
                    CategoryEntity("cat_drinks", "Cat-Tastic Drinks"),
                    CategoryEntity("combos", "Combos & Packages")
                )
                menuDao.insertCategories(categories)
            } catch (e: Exception) {
                android.util.Log.e("PosDatabase", "Error seeding categories", e)
            }

            try {
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
                menuDao.insertItems(items)
            } catch (e: Exception) {
                android.util.Log.e("PosDatabase", "Error seeding items", e)
            }

            try {
                val inventoryItems = listOf(
                    InventoryEntity("inv_cups", "Cups", "pcs", 100.0, 20.0),
                    InventoryEntity("inv_takoyaki", "Takoyaki Balls", "pcs", 100.0, 20.0),
                    InventoryEntity("inv_fries", "Potato Fries", "grams", 100.0, 10.0),
                    InventoryEntity("inv_nachos", "Nacho Chips", "grams", 100.0, 10.0)
                )
                inventoryDao.insertInventoryItems(inventoryItems)
            } catch (e: Exception) {
                android.util.Log.e("PosDatabase", "Error seeding inventory", e)
            }

            try {
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
            } catch (e: Exception) {
                android.util.Log.e("PosDatabase", "Error seeding recipe mappings", e)
            }
        }
    }
}
