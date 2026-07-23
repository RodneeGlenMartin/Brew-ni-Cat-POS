package com.example.cattasticpos.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cattasticpos.data.local.entity.AppConfigEntity

internal object DatabaseMigrationUtils {

    fun columnExists(db: SupportSQLiteDatabase, table: String, column: String): Boolean {
        db.query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            if (nameIndex < 0) return false
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == column) return true
            }
        }
        return false
    }

    fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean {
        db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(table)
        ).use { return it.moveToFirst() }
    }

    fun ordersPrimaryKeyIsInteger(db: SupportSQLiteDatabase): Boolean {
        if (!tableExists(db, "orders")) return false
        db.query("PRAGMA table_info(`orders`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            val typeIndex = cursor.getColumnIndex("type")
            if (nameIndex < 0 || typeIndex < 0) return false
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == "id") {
                    return cursor.getString(typeIndex).equals("INTEGER", ignoreCase = true)
                }
            }
        }
        return false
    }

    fun migrate10To11(db: SupportSQLiteDatabase) {
        if (!columnExists(db, "orders", "cashierId")) {
            db.execSQL("ALTER TABLE orders ADD COLUMN cashierId TEXT")
        }
        if (!columnExists(db, "orders", "tableLabel")) {
            db.execSQL("ALTER TABLE orders ADD COLUMN tableLabel TEXT")
        }
        if (!columnExists(db, "app_config", "cashiersJson")) {
            db.execSQL(
                "ALTER TABLE app_config ADD COLUMN cashiersJson TEXT NOT NULL DEFAULT '" +
                    AppConfigEntity.DEFAULT_CASHIERS_JSON.replace("'", "''") + "'"
            )
        }
        if (!tableExists(db, "void_records")) {
            db.execSQL(
                """
                CREATE TABLE void_records (
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

    fun migrate11To12(db: SupportSQLiteDatabase) {
        if (ordersPrimaryKeyIsInteger(db)) {
            if (!columnExists(db, "orders", "cashierName")) {
                db.execSQL("ALTER TABLE orders ADD COLUMN cashierName TEXT")
            }
            if (!tableExists(db, "void_records")) {
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
            updateSqliteSequence(db, "orders")
            updateSqliteSequence(db, "order_items")
            return
        }

        val cashierSelect = if (columnExists(db, "orders", "cashierId")) "cashierId" else "NULL"
        val tableLabelSelect = if (columnExists(db, "orders", "tableLabel")) "tableLabel" else "NULL"

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS orders_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                old_id TEXT,
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
            INSERT INTO orders_new (
                old_id, timestamp, subtotal, discountDeduction, discountLabel, total,
                paymentMethod, paymentReference, cashierId, cashierName, tableLabel
            )
            SELECT
                id, timestamp, subtotal, discountDeduction, discountLabel, total,
                paymentMethod, paymentReference, $cashierSelect, NULL, $tableLabelSelect
            FROM orders
            ORDER BY timestamp ASC, id ASC
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS order_items_new (
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
                FOREIGN KEY(orderId) REFERENCES orders_new(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        if (tableExists(db, "order_items")) {
            db.execSQL(
                """
                INSERT INTO order_items_new (
                    id, orderId, itemId, itemName, variantId, variantName, flavor,
                    quantity, unitPrice, totalPrice
                )
                SELECT
                    oi.id, o.id, oi.itemId, oi.itemName, oi.variantId, oi.variantName, oi.flavor,
                    oi.quantity, oi.unitPrice, oi.totalPrice
                FROM order_items oi
                INNER JOIN orders_new o ON o.old_id = oi.orderId
                """.trimIndent()
            )
        }

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS order_id_map (
                old_id TEXT NOT NULL PRIMARY KEY,
                new_id INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO order_id_map (old_id, new_id)
            SELECT old_id, id FROM orders_new WHERE old_id IS NOT NULL
            """.trimIndent()
        )

        val hasLegacyVoidRecords = tableExists(db, "void_records")
        if (hasLegacyVoidRecords) {
            db.execSQL("ALTER TABLE void_records RENAME TO void_records_legacy")
        }

        db.execSQL("DROP TABLE IF EXISTS order_items")
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
            INSERT INTO orders (
                id, timestamp, subtotal, discountDeduction, discountLabel, total,
                paymentMethod, paymentReference, cashierId, cashierName, tableLabel
            )
            SELECT
                id, timestamp, subtotal, discountDeduction, discountLabel, total,
                paymentMethod, paymentReference, cashierId, cashierName, tableLabel
            FROM orders_new
            """.trimIndent()
        )
        db.execSQL("DROP TABLE IF EXISTS orders_new")

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
        db.execSQL(
            """
            INSERT INTO order_items (
                id, orderId, itemId, itemName, variantId, variantName, flavor,
                quantity, unitPrice, totalPrice
            )
            SELECT
                id, orderId, itemId, itemName, variantId, variantName, flavor,
                quantity, unitPrice, totalPrice
            FROM order_items_new
            """.trimIndent()
        )
        db.execSQL("DROP TABLE IF EXISTS order_items_new")
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
        if (hasLegacyVoidRecords) {
            db.execSQL(
                """
                INSERT INTO void_records (
                    id, orderId, reason, timestamp, cashierId, orderTotal
                )
                SELECT
                    vr.id, m.new_id, vr.reason, vr.timestamp, vr.cashierId, vr.orderTotal
                FROM void_records_legacy vr
                INNER JOIN order_id_map m ON m.old_id = vr.orderId
                """.trimIndent()
            )
            db.execSQL("DROP TABLE IF EXISTS void_records_legacy")
        }
        db.execSQL("DROP TABLE IF EXISTS order_id_map")

        updateSqliteSequence(db, "orders")
        updateSqliteSequence(db, "order_items")
    }

    private fun updateSqliteSequence(db: SupportSQLiteDatabase, table: String) {
        if (!tableExists(db, table)) return
        db.execSQL(
            """
            INSERT OR REPLACE INTO sqlite_sequence (name, seq)
            SELECT '$table', IFNULL(MAX(id), 0) FROM `$table`
            """.trimIndent()
        )
    }
}
