package com.example.inventory.data.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.DatabaseHelper

open class BaseRepository(context: Context) {
    protected val db: SQLiteDatabase by lazy {
        DatabaseHelper.getInstance(context).writableDatabase
    }
}
