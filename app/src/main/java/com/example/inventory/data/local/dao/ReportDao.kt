package com.example.inventory.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.InventoryLog

class ReportDao(private val db: SQLiteDatabase) {

    fun getInventoryLogs(productId: Long? = null, limit: Int = 100): List<InventoryLog> {
        val list = mutableListOf<InventoryLog>()
        val sql = if (productId != null)
            "SELECT * FROM inventory_log WHERE product_id = ? ORDER BY created_at DESC LIMIT ?"
        else
            "SELECT * FROM inventory_log ORDER BY created_at DESC LIMIT ?"
        val args = if (productId != null) arrayOf(productId.toString(), limit.toString())
                    else arrayOf(limit.toString())

        db.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(InventoryLog(
                    id = cursor.getLong(0), productId = cursor.getLong(1),
                    delta = cursor.getDouble(2), beforeQty = cursor.getDouble(3),
                    afterQty = cursor.getDouble(4), remark = cursor.getString(5),
                    createdAt = cursor.getString(6)
                ))
            }
        }
        return list
    }

    fun getPurchaseTotal(supplier: String? = null): Double {
        var total = 0.0
        val sql = if (supplier != null)
            "SELECT COALESCE(SUM(total_amount), 0) FROM purchase_orders WHERE supplier = ? AND status = 'received'"
        else
            "SELECT COALESCE(SUM(total_amount), 0) FROM purchase_orders WHERE status = 'received'"
        val args = if (supplier != null) arrayOf(supplier) else null
        db.rawQuery(sql, args).use { cursor ->
            if (cursor.moveToFirst()) total = cursor.getDouble(0)
        }
        return total
    }

    fun getSalesTotal(customer: String? = null): Double {
        var total = 0.0
        val sql = if (customer != null)
            "SELECT COALESCE(SUM(total_amount), 0) FROM sales_orders WHERE customer = ? AND status = 'shipped'"
        else
            "SELECT COALESCE(SUM(total_amount), 0) FROM sales_orders WHERE status = 'shipped'"
        val args = if (customer != null) arrayOf(customer) else null
        db.rawQuery(sql, args).use { cursor ->
            if (cursor.moveToFirst()) total = cursor.getDouble(0)
        }
        return total
    }

    fun getMonthlyStats(): List<Triple<String, Double, Double>> {
        val list = mutableListOf<Triple<String, Double, Double>>()
        db.rawQuery("""
            SELECT substr(order_date, 1, 7) as month,
                   COALESCE(SUM(CASE WHEN status = 'received' THEN total_amount ELSE 0 END), 0) as purchase,
                   COALESCE(SUM(CASE WHEN status = 'shipped' THEN total_amount ELSE 0 END), 0) as sales
            FROM (SELECT order_date, status, total_amount FROM purchase_orders
                  UNION ALL
                  SELECT order_date, status, total_amount FROM sales_orders)
            GROUP BY month
            ORDER BY month DESC
            LIMIT 12
        """.trimIndent(), null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(Triple(cursor.getString(0), cursor.getDouble(1), cursor.getDouble(2)))
            }
        }
        return list
    }
}
