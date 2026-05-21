package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.InventoryDao
import com.example.inventory.data.local.model.InventorySummary

class InventoryRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { InventoryDao(db) }

    fun getInventorySummary(keyword: String = ""): List<InventorySummary> = dao.getInventorySummary(keyword)

    fun getLowStockAlerts(): List<InventorySummary> = dao.getLowStockAlerts()

    fun getTotalStats(): Triple<Int, Double, Double> = dao.getTotalStats()
}
