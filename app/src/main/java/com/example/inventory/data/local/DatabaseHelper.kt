package com.example.inventory.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "inventory.db"
        private const val DB_VERSION = 1

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }
        }

        fun copyDatabaseFromAssets(context: Context) {
            val dbPath = context.getDatabasePath(DB_NAME)
            if (dbPath.exists()) return

            dbPath.parentFile?.mkdirs()
            context.assets.open(DB_NAME).use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Database is copied from assets, no creation needed
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle future upgrades if needed
    }
}
