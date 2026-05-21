package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.DatabaseHelper
import com.example.inventory.data.local.dao.*
import com.example.inventory.data.local.model.*

open class BaseRepository(context: Context) {
    protected val db by lazy {
        DatabaseHelper.getInstance(context).readableDatabase
    }
}
