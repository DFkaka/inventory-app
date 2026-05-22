package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.PurchaseDao
import com.example.inventory.data.local.model.PurchaseOrder
import com.example.inventory.data.local.model.PurchaseOrderItem

class PurchaseRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { PurchaseDao(db) }

    fun getAllOrders(keyword: String = "", status: String = "", dateFrom: String = "", dateTo: String = ""): List<PurchaseOrder> =
        dao.getAll(keyword, status, dateFrom, dateTo)
    fun getOrderById(id: Long): PurchaseOrder? = dao.getById(id)
    fun getItems(orderId: Long): List<PurchaseOrderItem> = dao.getItems(orderId)
    fun insert(orderNo: String, supplier: String, orderDate: String, totalAmount: Double = 0.0, status: String = "draft", note: String = ""): Long =
        dao.insert(orderNo, supplier, orderDate, totalAmount, status, note)
    fun insertItem(orderId: Long, productId: Long, quantity: Double, unitPrice: Double, barcode: String = "", unit: String = "个", spec: String = "") =
        dao.insertItem(orderId, productId, quantity, unitPrice, barcode, unit, spec)
    fun deleteItem(itemId: Long) = dao.deleteItem(itemId)
    fun updateItem(itemId: Long, quantity: Double, unitPrice: Double) = dao.updateItem(itemId, quantity, unitPrice)
    fun updateStatus(orderId: Long, status: String) = dao.updateStatus(orderId, status)
    fun getLastPrice(supplierName: String, productCode: String): Double? = dao.getLastPrice(supplierName, productCode)
    fun updateTotalAmount(orderId: Long, totalAmount: Double) = dao.updateTotalAmount(orderId, totalAmount)
}
