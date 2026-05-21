package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.SalesDao
import com.example.inventory.data.local.model.SalesOrder
import com.example.inventory.data.local.model.SalesOrderItem

class SalesRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { SalesDao(db) }

    fun getAllOrders(keyword: String = "", status: String = "", dateFrom: String = "", dateTo: String = ""): List<SalesOrder> =
        dao.getAll(keyword, status, dateFrom, dateTo)
    fun getOrderById(id: Long): SalesOrder? = dao.getById(id)
    fun getItems(orderId: Long): List<SalesOrderItem> = dao.getItems(orderId)
    fun insert(orderNo: String, customer: String, orderDate: String, totalAmount: Double = 0.0, status: String = "draft", note: String = ""): Long =
        dao.insert(orderNo, customer, orderDate, totalAmount, status, note)
    fun insertItem(orderId: Long, productId: Long, quantity: Double, unitPrice: Double, barcode: String = "", unit: String = "个", spec: String = "") =
        dao.insertItem(orderId, productId, quantity, unitPrice, barcode, unit, spec)
    fun deleteItem(itemId: Long) = dao.deleteItem(itemId)
    fun updateTotalAmount(orderId: Long, totalAmount: Double) = dao.updateTotalAmount(orderId, totalAmount)
}
