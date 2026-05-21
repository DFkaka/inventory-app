package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.ReportDao
import com.example.inventory.data.local.model.InventoryLog
import com.example.inventory.data.local.model.RecentBizRecord

class ReportRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { ReportDao(db) }

    fun getRecentBizRecords(limit: Int = 5): List<RecentBizRecord> = dao.getRecentBizRecords(limit)

    fun getInventoryLogs(productId: Long? = null, limit: Int = 100): List<InventoryLog> = dao.getInventoryLogs(productId, limit)

    fun getPurchaseTotal(supplier: String? = null): Double = dao.getPurchaseTotal(supplier)

    fun getSalesTotal(customer: String? = null): Double = dao.getSalesTotal(customer)

    fun getMonthlyStats(): List<Triple<String, Double, Double>> = dao.getMonthlyStats()
}
