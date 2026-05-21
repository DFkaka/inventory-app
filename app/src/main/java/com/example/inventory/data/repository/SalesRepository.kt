package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.SalesDao
import com.example.inventory.data.local.model.SalesOrder

class SalesRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { SalesDao(db) }

    fun getAllOrders(
        keyword: String = "",
        status: String = "",
        dateFrom: String = "",
        dateTo: String = ""
    ): List<SalesOrder> = dao.getAll(keyword, status, dateFrom, dateTo)

    fun getOrdersByStatus(status: String): List<SalesOrder> = dao.getByStatus(status)

    fun getUnpaidOrders(): List<SalesOrder> = dao.getUnpaid()

    fun getOrderById(id: Long): SalesOrder? = dao.getById(id)
}
