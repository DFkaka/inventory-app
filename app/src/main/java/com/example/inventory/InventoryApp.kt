package com.example.inventory

import android.app.Application
import com.example.inventory.data.local.DatabaseHelper

class InventoryApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Copy database from assets to app databases directory on first launch
        DatabaseHelper.copyDatabaseFromAssets(this)
    }
}
