package com.example.inventory.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.SalesOrder

class SalesDao(private val db: SQLiteDatabase) {

    fun getAll(
        keyword: String = "",
        status: String = "",
        dateFrom: String = "",
        dateTo: String = ""
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
            when (status) {
                "草稿" -> conditions.add("so.status = 'draft'")
                "已审核" -> conditions.add("so.status = 'shipped'")
            }
        }
        if (dateFrom.isNotBlank()) {
            conditions.add("so.order_date >= ?")
            args.add(dateFrom)
        }
        if (dateTo.isNotBlank()) {
            conditions.add("so.order_date <= ?")
            args.add(dateTo)
        }

        val where = if (conditions.isEmpty()) "" else "WHERE " + conditions.joinToString(" AND ")
        val sql = """
            SELECT so.* FROM sales_orders so
            LEFT JOIN customers c ON so.customer = c.name
            $where
            ORDER BY so.order_date DESC
        """.trimIndent()

        db.rawQuery(sql, args.toTypedArray()).use { cursor ->
            while (cursor.moveToNext()) list.add(mapOrder(cursor))
        }
        return list
    }

    fun getByStatus(status: String): List<SalesOrder> {
        val list = mutableListOf<SalesOrder>()
        db.rawQuery("SELECT * FROM sales_orders WHERE status = ? ORDER BY order_date DESC", arrayOf(status)).use { cursor ->
            while (cursor.moveToNext()) list.add(mapOrder(cursor))
        }
        return list
    }

    fun getUnpaid(): List<SalesOrder> {
        val list = mutableListOf<SalesOrder>()
        db.rawQuery("SELECT * FROM sales_orders WHERE payment_status = '未结完' ORDER BY order_date DESC", null).use { cursor ->
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
