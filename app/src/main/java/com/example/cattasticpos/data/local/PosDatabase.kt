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
                val appContext = context.applicationContext
                DatabaseSafetyBackup.prepareForUpgrade(appContext)
                val instance = buildDatabase(appContext, scope)
                INSTANCE = instance
                try {
                    instance.openHelper.writableDatabase
                } catch (e: Exception) {
                    INSTANCE = null
                    DatabaseSafetyBackup.markMigrationFailed(appContext)
                    android.util.Log.e("PosDatabase", "Database open/migration failed", e)
                    throw e
                }
                instance
            }
        }

        private fun buildDatabase(appContext: Context, scope: CoroutineScope): PosDatabase {
            return Room.databaseBuilder(
                appContext,
                PosDatabase::class.java,
                "pos_database"
            )
                .addMigrations(
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_10_12
                )
                .addCallback(PosDatabaseCallback(scope))
                .addCallback(MigrationSuccessCallback(appContext))
                .build()
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

        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                DatabaseMigrationUtils.migrate10To11(db)
            }
        }

        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                DatabaseMigrationUtils.migrate11To12(db)
            }
        }

        /** Single-hop path for users still on v10 — preserves all orders in one migration. */
        val MIGRATION_10_12 = object : androidx.room.migration.Migration(10, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                DatabaseMigrationUtils.migrate10To11(db)
                DatabaseMigrationUtils.migrate11To12(db)
            }
        }
    }

    private class MigrationSuccessCallback(
        private val appContext: Context
    ) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            DatabaseSafetyBackup.markMigrationSucceeded(appContext)
        }
    }

    private class PosDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            seedIfNeeded()
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            seedIfNeeded()
        }

        private fun seedIfNeeded() {
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    DatabaseSeeder.seedDefaultsIfMissing(
                        database.menuDao(),
                        database.inventoryDao(),
                        database.recipeDao(),
                        database.appConfigDao()
                    )
                }
            }
        }
    }
}
