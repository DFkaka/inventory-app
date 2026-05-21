package com.example.inventory.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.InventorySummary

class InventoryDao(private val db: SQLiteDatabase) {

    fun getInventorySummary(keyword: String = ""): List<InventorySummary> {
        val list = mutableListOf<InventorySummary>()
        val sql = """
            SELECT p.id, p.code, p.name, p.barcode,
                   COALESCE(c.name, '') as category_name,
                   p.unit, p.spec,
                   COALESCE(inv.quantity, 0) as quantity,
                   COALESCE(inv.safety_stock, 0) as safety_stock,
                   p.cost_price, p.retail_price,
                   COALESCE(inv.quantity, 0) * p.cost_price as total_cost,
                   COALESCE(inv.quantity, 0) * p.retail_price as total_retail
            FROM products p
            LEFT JOIN inventory inv ON p.id = inv.product_id
            LEFT JOIN categories c ON p.category_id = c.id
            ${if (keyword.isNotBlank()) "WHERE p.code LIKE ? OR p.name LIKE ? OR p.pinyin_code LIKE ? OR p.barcode LIKE ?" else ""}
            ORDER BY p.code
        """.trimIndent()

        val args = if (keyword.isNotBlank()) {
            val like = "%$keyword%"
            arrayOf(like, like, like, like)
        } else null

        db.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(InventorySummary(
                    productId = cursor.getLong(0), code = cursor.getString(1),
                    name = cursor.getString(2), barcode = cursor.getString(3),
                    categoryName = cursor.getString(4), unit = cursor.getString(5),
                    spec = cursor.getString(6), quantity = cursor.getDouble(7),
                    safetyStock = cursor.getDouble(8), costPrice = cursor.getDouble(9),
                    retailPrice = cursor.getDouble(10), totalCost = cursor.getDouble(11),
                    totalRetail = cursor.getDouble(12)
                ))
            }
        }
        return list
    }

    fun getLowStockAlerts(): List<InventorySummary> {
        val list = mutableListOf<InventorySummary>()
        db.rawQuery("""
            SELECT p.id, p.code, p.name, p.barcode,
                   COALESCE(c.name, '') as category_name,
                   p.unit, p.spec,
                   COALESCE(inv.quantity, 0) as quantity,
                   COALESCE(inv.safety_stock, 0) as safety_stock,
                   p.cost_price, p.retail_price,
                   COALESCE(inv.quantity, 0) * p.cost_price as total_cost,
                   COALESCE(inv.quantity, 0) * p.retail_price as total_retail
            FROM products p
            LEFT JOIN inventory inv ON p.id = inv.product_id
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE COALESCE(inv.quantity, 0) <= COALESCE(inv.safety_stock, 0)
            ORDER BY p.code
        """.trimIndent(), null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(InventorySummary(
                    productId = cursor.getLong(0), code = cursor.getString(1),
                    name = cursor.getString(2), barcode = cursor.getString(3),
                    categoryName = cursor.getString(4), unit = cursor.getString(5),
                    spec = cursor.getString(6), quantity = cursor.getDouble(7),
                    safetyStock = cursor.getDouble(8), costPrice = cursor.getDouble(9),
                    retailPrice = cursor.getDouble(10), totalCost = cursor.getDouble(11),
                    totalRetail = cursor.getDouble(12)
                ))
            }
        }
        return list
    }

    fun getTotalStats(): Triple<Int, Double, Double> {
        var productCount = 0
        var totalCost = 0.0
        var totalRetail = 0.0
        db.rawQuery("""
            SELECT COUNT(*) as cnt,
                   COALESCE(SUM(inv.quantity * p.cost_price), 0) as total_cost,
                   COALESCE(SUM(inv.quantity * p.retail_price), 0) as total_retail
            FROM inventory inv
            JOIN products p ON inv.product_id = p.id
        """.trimIndent(), null).use { cursor ->
            if (cursor.moveToFirst()) {
                productCount = cursor.getInt(0)
                totalCost = cursor.getDouble(1)
                totalRetail = cursor.getDouble(2)
            }
        }
        return Triple(productCount, totalCost, totalRetail)
    }
}
