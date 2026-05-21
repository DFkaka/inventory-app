package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.PurchaseDao
import com.example.inventory.data.local.model.PurchaseOrder

class PurchaseRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { PurchaseDao(db) }

    fun getAllOrders(keyword: String = "", status: String = "", dateFrom: String = "", dateTo: String = ""): List<PurchaseOrder> =
        dao.getAll(keyword, status, dateFrom, dateTo)
    fun getOrderById(id: Long): PurchaseOrder? = dao.getById(id)
    fun insert(orderNo: String, supplier: String, orderDate: String, totalAmount: Double = 0.0,
               status: String = "draft", note: String = ""): Long =
        dao.insert(orderNo, supplier, orderDate, totalAmount, status, note)
    fun insertItem(orderId: Long, productId: Long, quantity: Double, unitPrice: Double,
                   barcode: String = "", unit: String = "个", spec: String = "") =
        dao.insertItem(orderId, productId, quantity, unitPrice, barcode, unit, spec)
}
