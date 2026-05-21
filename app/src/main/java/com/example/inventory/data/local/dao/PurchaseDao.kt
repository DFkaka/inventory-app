package com.example.inventory.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.PurchaseOrder

class PurchaseDao(private val db: SQLiteDatabase) {

    fun getAll(
        keyword: String = "",
        status: String = "",
        dateFrom: String = "",
        dateTo: String = ""
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
        if (dateFrom.isNotBlank()) {
            conditions.add("po.order_date >= ?")
            args.add(dateFrom)
        }
        if (dateTo.isNotBlank()) {
            conditions.add("po.order_date <= ?")
            args.add(dateTo)
        }

        val where = if (conditions.isEmpty()) "" else "WHERE " + conditions.joinToString(" AND ")
        val sql = """
            SELECT po.* FROM purchase_orders po
            LEFT JOIN suppliers s ON po.supplier = s.name
            $where
            ORDER BY po.order_date DESC
        """.trimIndent()

        db.rawQuery(sql, args.toTypedArray()).use { cursor ->
            while (cursor.moveToNext()) list.add(mapOrder(cursor))
        }
        return list
    }

    fun getByStatus(status: String): List<PurchaseOrder> {
        val list = mutableListOf<PurchaseOrder>()
        db.rawQuery("SELECT * FROM purchase_orders WHERE status = ? ORDER BY order_date DESC", arrayOf(status)).use { cursor ->
            while (cursor.moveToNext()) list.add(mapOrder(cursor))
        }
        return list
    }

    fun getUnpaid(): List<PurchaseOrder> {
        val list = mutableListOf<PurchaseOrder>()
        db.rawQuery("SELECT * FROM purchase_orders WHERE payment_status = '未结完' ORDER BY order_date DESC", null).use { cursor ->
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
