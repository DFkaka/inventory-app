package com.example.inventory.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.*

class ProductDao(private val db: SQLiteDatabase) {

    fun getAll(): List<Product> {
        val list = mutableListOf<Product>()
        db.rawQuery("SELECT * FROM products ORDER BY code", null).use { cursor ->
            while (cursor.moveToNext()) list.add(mapCursor(cursor))
        }
        return list
    }

    fun search(keyword: String): List<Product> {
        val list = mutableListOf<Product>()
        val like = "%$keyword%"
        db.rawQuery(
            "SELECT * FROM products WHERE code LIKE ? OR name LIKE ? OR pinyin_code LIKE ? OR barcode LIKE ? ORDER BY code",
            arrayOf(like, like, like, like)
        ).use { cursor ->
            while (cursor.moveToNext()) list.add(mapCursor(cursor))
        }
        return list
    }

    fun getByCategory(categoryId: Long): List<Product> {
        val list = mutableListOf<Product>()
        db.rawQuery("SELECT * FROM products WHERE category_id = ? ORDER BY code", arrayOf(categoryId.toString())).use { cursor ->
            while (cursor.moveToNext()) list.add(mapCursor(cursor))
        }
        return list
    }

    fun getById(id: Long): Product? {
        db.rawQuery("SELECT * FROM products WHERE id = ?", arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) return mapCursor(cursor)
        }
        return null
    }

    fun getCategories(): List<Category> {
        val list = mutableListOf<Category>()
        db.rawQuery("SELECT * FROM categories ORDER BY id", null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(Category(
                    id = cursor.getLong(0), name = cursor.getString(1),
                    parentId = if (cursor.isNull(2)) null else cursor.getLong(2),
                    createdAt = cursor.getString(3)
                ))
            }
        }
        return list
    }

    fun insert(code: String, name: String, barcode: String = "", pinyinCode: String = "",
               categoryId: Long? = null, unit: String = "个", spec: String = "",
               retailPrice: Double = 0.0, wholesalePrice: Double = 0.0, costPrice: Double = 0.0,
               supplierCode: String = ""): Long {
        val cv = ContentValues().apply {
            put("code", code)
            put("name", name)
            put("barcode", barcode)
            put("pinyin_code", pinyinCode)
            if (categoryId != null) put("category_id", categoryId)
            put("unit", unit)
            put("spec", spec)
            put("retail_price", retailPrice)
            put("wholesale_price", wholesalePrice)
            put("cost_price", costPrice)
            put("supplier_code", supplierCode)
        }
        return db.insert("products", null, cv)
    }

    private fun mapCursor(cursor: android.database.Cursor): Product {
        return Product(
            id = cursor.getLong(0), code = cursor.getString(1), barcode = cursor.getString(2),
            name = cursor.getString(3), pinyinCode = cursor.getString(4),
            categoryId = if (cursor.isNull(5)) null else cursor.getLong(5),
            unit = cursor.getString(6), spec = cursor.getString(7),
            retailPrice = cursor.getDouble(8), wholesalePrice = cursor.getDouble(9),
            costPrice = cursor.getDouble(10), supplierCode = cursor.getString(11),
            createdAt = cursor.getString(12)
        )
    }
}
