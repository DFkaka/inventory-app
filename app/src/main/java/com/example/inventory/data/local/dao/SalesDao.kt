package com.example.inventory.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.SalesOrder

class SalesDao(private val db: SQLiteDatabase) {

    fun getAll(keyword: String = ""): List<SalesOrder> {
        val list = mutableListOf<SalesOrder>()
        val sql = if (keyword.isNotBlank())
            "SELECT * FROM sales_orders WHERE order_no LIKE ? OR customer LIKE ? ORDER BY order_date DESC"
        else
            "SELECT * FROM sales_orders ORDER BY order_date DESC"
        val args = if (keyword.isNotBlank()) {
            val like = "%$keyword%"
            arrayOf(like, like)
        } else null

        db.rawQuery(sql, args).use { cursor ->
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
