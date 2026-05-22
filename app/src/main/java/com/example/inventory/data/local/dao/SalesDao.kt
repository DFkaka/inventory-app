package com.example.inventory.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.SalesOrder
import com.example.inventory.data.local.model.SalesOrderItem

class SalesDao(private val db: SQLiteDatabase) {

    fun getAll(
        keyword: String = "", status: String = "", dateFrom: String = "", dateTo: String = ""
    ): List<SalesOrder> {
        val list = mutableListOf<SalesOrder>()
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (keyword.isNotBlank()) {
            val like = "%$keyword%"
            conditions.add("(so.order_no LIKE ? OR so.customer LIKE ? OR c.code LIKE ? OR c.name LIKE ?)")
            args.addAll(listOf(like, like, like, like))
        }
        if (status.isNotBlank()) {
            when (status) { "草稿" -> conditions.add("so.status = 'draft'") ; "已审核" -> conditions.add("so.status = 'shipped'") }
        }
        if (dateFrom.isNotBlank()) { conditions.add("so.order_date >= ?"); args.add(dateFrom) }
        if (dateTo.isNotBlank()) { conditions.add("so.order_date <= ?"); args.add(dateTo) }

        val where = if (conditions.isEmpty()) "" else "WHERE " + conditions.joinToString(" AND ")
        val sql = "SELECT so.* FROM sales_orders so LEFT JOIN customers c ON so.customer = c.name $where ORDER BY so.order_date DESC"
        db.rawQuery(sql, args.toTypedArray()).use { cursor ->
            while (cursor.moveToNext()) list.add(mapOrder(cursor))
        }
        return list
    }

    fun getById(id: Long): SalesOrder? {
        db.rawQuery("SELECT * FROM sales_orders WHERE id = ?", arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) return mapOrder(cursor)
        }
        return null
    }

    fun getItems(orderId: Long): List<SalesOrderItem> {
        val list = mutableListOf<SalesOrderItem>()
        db.rawQuery("SELECT * FROM sales_order_items WHERE order_id = ?", arrayOf(orderId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(SalesOrderItem(
                    id = cursor.getLong(0), orderId = cursor.getLong(1),
                    productId = cursor.getLong(2), quantity = cursor.getDouble(3),
                    unitPrice = cursor.getDouble(4), subtotal = cursor.getDouble(5),
                    barcode = cursor.getString(6), unit = cursor.getString(7),
                    spec = cursor.getString(8)
                ))
            }
        }
        return list
    }

    fun insert(orderNo: String, customer: String, orderDate: String, totalAmount: Double = 0.0,
               status: String = "draft", note: String = ""): Long {
        val cv = ContentValues().apply {
            put("order_no", orderNo); put("customer", customer); put("order_date", orderDate)
            put("status", status); put("total_amount", totalAmount); put("paid_amount", 0)
            put("payment_status", "未结完"); put("note", note)
        }
        return db.insert("sales_orders", null, cv)
    }

    fun insertItem(orderId: Long, productId: Long, quantity: Double, unitPrice: Double,
                   barcode: String = "", unit: String = "个", spec: String = "") {
        val cv = ContentValues().apply {
            put("order_id", orderId); put("product_id", productId)
            put("quantity", quantity); put("unit_price", unitPrice)
            put("subtotal", quantity * unitPrice); put("barcode", barcode)
            put("unit", unit); put("spec", spec)
        }
        db.insert("sales_order_items", null, cv)
    }

    

    fun updateItem(itemId: Long, quantity: Double, unitPrice: Double) {
        val cv = ContentValues().apply {
            put("quantity", quantity)
            put("unit_price", unitPrice)
            put("subtotal", quantity * unitPrice)
        }
        db.update("sales_order_items", cv, "id = ?", arrayOf(itemId.toString()))
    }fun deleteItem(itemId: Long) {
        db.delete("sales_order_items", "id = ?", arrayOf(itemId.toString()))
    }

    
    fun updateStatus(orderId: Long, status: String) {
        val cv = ContentValues().apply { put("status", status) }
        db.update("sales_orders", cv, "id = ?", arrayOf(orderId.toString()))
    }
fun updateTotalAmount(orderId: Long, totalAmount: Double) {
        val cv = ContentValues().apply { put("total_amount", totalAmount) }
        db.update("sales_orders", cv, "id = ?", arrayOf(orderId.toString()))
    }

    private fun mapOrder(cursor: android.database.Cursor): SalesOrder {
        return SalesOrder(
            id = cursor.getLong(0), orderNo = cursor.getString(1),
            customer = cursor.getString(2), orderDate = cursor.getString(3),
            status = cursor.getString(4), totalAmount = cursor.getDouble(5),
            paidAmount = cursor.getDouble(6), paymentStatus = cursor.getString(7),
            note = cursor.getString(8), createdAt = cursor.getString(9)
        )
    }
}
