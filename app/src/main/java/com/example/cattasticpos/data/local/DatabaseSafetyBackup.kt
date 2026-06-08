package com.example.cattasticpos.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import java.io.File

/**
 * Copies the Room database to a local backup before an app upgrade migration runs.
 * If migration fails, the backup is restored on the next launch so transactions are not lost.
 */
internal object DatabaseSafetyBackup {
    private const val DB_NAME = "pos_database"
    private const val PREFS = "db_safety_prefs"
    private const val KEY_LAST_OK_VERSION = "last_ok_app_version"
    private const val KEY_MIGRATION_FAILED = "migration_failed"
    private const val TAG = "DatabaseSafetyBackup"

    private fun backupFile(context: Context): File =
        File(context.applicationContext.filesDir, "$DB_NAME.pre_upgrade")

    fun prepareForUpgrade(context: Context) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastOk = prefs.getInt(KEY_LAST_OK_VERSION, 0)
        val current = installedVersionCode(appContext)
        if (current <= lastOk) return

        val dbFile = appContext.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            prefs.edit().putInt(KEY_LAST_OK_VERSION, current).apply()
            return
        }

        if (prefs.getBoolean(KEY_MIGRATION_FAILED, false)) {
            restoreFromBackup(appContext)
            prefs.edit().putBoolean(KEY_MIGRATION_FAILED, false).apply()
        }

        try {
            checkpointDatabase(dbFile)
            dbFile.copyTo(backupFile(appContext), overwrite = true)
            Log.i(TAG, "Pre-upgrade database backup saved")
        } catch (e: Exception) {
            Log.e(TAG, "Could not create pre-upgrade backup", e)
        }
    }

    fun markMigrationSucceeded(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_LAST_OK_VERSION, installedVersionCode(context))
            .putBoolean(KEY_MIGRATION_FAILED, false)
            .apply()
    }

    fun markMigrationFailed(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MIGRATION_FAILED, true)
            .apply()
    }

    private fun restoreFromBackup(context: Context) {
        val backup = backupFile(context)
        if (!backup.exists()) {
            Log.w(TAG, "No pre-upgrade backup to restore")
            return
        }
        val dbFile = context.getDatabasePath(DB_NAME)
        dbFile.parentFile?.mkdirs()
        backup.copyTo(dbFile, overwrite = true)
        File(dbFile.parent, "$DB_NAME-wal").delete()
        File(dbFile.parent, "$DB_NAME-shm").delete()
        Log.i(TAG, "Restored database from pre-upgrade backup")
    }

    private fun checkpointDatabase(dbFile: File) {
        SQLiteDatabase.openDatabase(
            dbFile.path,
            null,
            SQLiteDatabase.OPEN_READWRITE
        ).use { db ->
            db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).close()
        }
    }

    private fun installedVersionCode(context: Context): Int {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            info.versionCode
        }
    }
}
