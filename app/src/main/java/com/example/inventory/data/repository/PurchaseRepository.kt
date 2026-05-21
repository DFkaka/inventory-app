package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.PurchaseDao
import com.example.inventory.data.local.model.PurchaseOrder

class PurchaseRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { PurchaseDao(db) }

    fun getAllOrders(keyword: String = ""): List<PurchaseOrder> = dao.getAll(keyword)

    fun getOrdersByStatus(status: String): List<PurchaseOrder> = dao.getByStatus(status)

    fun getUnpaidOrders(): List<PurchaseOrder> = dao.getUnpaid()

    fun getOrderById(id: Long): PurchaseOrder? = dao.getById(id)
}
