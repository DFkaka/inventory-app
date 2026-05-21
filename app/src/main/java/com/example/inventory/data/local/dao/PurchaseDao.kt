package com.example.inventory.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.PurchaseOrder

class PurchaseDao(private val db: SQLiteDatabase) {

    fun getAll(
        keyword: String = "", status: String = "", dateFrom: String = "", dateTo: String = ""
    ): List<PurchaseOrder> {
        val list = mutableListOf<PurchaseOrder>()
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (keyword.isNotBlank()) {
            val like = "%$keyword%"
            conditions.add("(po.order_no LIKE ? OR po.supplier LIKE ? OR s.code LIKE ? OR s.name LIKE ?)")
            args.addAll(listOf(like, like, like, like))
        }
        if (status.isNotBlank()) {
            when (status) {
                "草稿" -> conditions.add("po.status = 'draft'")
                "已审核" -> conditions.add("po.status = 'received'")
            }
        }
        if (dateFrom.isNotBlank()) { conditions.add("po.order_date >= ?"); args.add(dateFrom) }
        if (dateTo.isNotBlank()) { conditions.add("po.order_date <= ?"); args.add(dateTo) }

        val where = if (conditions.isEmpty()) "" else "WHERE " + conditions.joinToString(" AND ")
        val sql = "SELECT po.* FROM purchase_orders po LEFT JOIN suppliers s ON po.supplier = s.name $where ORDER BY po.order_date DESC"
        db.rawQuery(sql, args.toTypedArray()).use { cursor ->
            while (cursor.moveToNext()) list.add(mapOrder(cursor))
        }
        return list
    }

    fun getById(id: Long): PurchaseOrder? {
        db.rawQuery("SELECT * FROM purchase_orders WHERE id = ?", arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) return mapOrder(cursor)
        }
        return null
    }

    fun insert(orderNo: String, supplier: String, orderDate: String, totalAmount: Double = 0.0,
               status: String = "draft", note: String = ""): Long {
        val cv = ContentValues().apply {
            put("order_no", orderNo)
            put("supplier", supplier)
            put("order_date", orderDate)
            put("status", status)
            put("total_amount", totalAmount)
            put("paid_amount", 0)
            put("payment_status", "未结完")
            put("note", note)
        }
        return db.insert("purchase_orders", null, cv)
    }

    fun insertItem(orderId: Long, productId: Long, quantity: Double, unitPrice: Double,
                   barcode: String = "", unit: String = "个", spec: String = "") {
        val cv = ContentValues().apply {
            put("order_id", orderId)
            put("product_id", productId)
            put("quantity", quantity)
            put("unit_price", unitPrice)
            put("subtotal", quantity * unitPrice)
            put("barcode", barcode)
            put("unit", unit)
            put("spec", spec)
        }
        db.insert("purchase_order_items", null, cv)
    }

    fun getMaxId(): Long {
        db.rawQuery("SELECT MAX(id) FROM purchase_orders", null).use { cursor ->
            if (cursor.moveToFirst()) return cursor.getLong(0)
        }
        return 0
    }

    private fun mapOrder(cursor: android.database.Cursor): PurchaseOrder {
        return PurchaseOrder(
            id = cursor.getLong(0), orderNo = cursor.getString(1),
            supplier = cursor.getString(2), orderDate = cursor.getString(3),
            status = cursor.getString(4), totalAmount = cursor.getDouble(5),
            paidAmount = cursor.getDouble(6), paymentStatus = cursor.getString(7),
            note = cursor.getString(8), createdAt = cursor.getString(9)
        )
    }
}
