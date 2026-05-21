package com.example.inventory.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.*

class ProductDao(private val db: SQLiteDatabase) {

    fun getAll(): List<Product> {
        val list = mutableListOf<Product>()
        db.rawQuery("SELECT * FROM products ORDER BY code", null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(Product(
                    id = cursor.getLong(0),
                    code = cursor.getString(1),
                    barcode = cursor.getString(2),
                    name = cursor.getString(3),
                    pinyinCode = cursor.getString(4),
                    categoryId = if (cursor.isNull(5)) null else cursor.getLong(5),
                    unit = cursor.getString(6),
                    spec = cursor.getString(7),
                    retailPrice = cursor.getDouble(8),
                    wholesalePrice = cursor.getDouble(9),
                    costPrice = cursor.getDouble(10),
                    supplierCode = cursor.getString(11),
                    createdAt = cursor.getString(12)
                ))
            }
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
            while (cursor.moveToNext()) {
                list.add(Product(
                    id = cursor.getLong(0),
                    code = cursor.getString(1),
                    barcode = cursor.getString(2),
                    name = cursor.getString(3),
                    pinyinCode = cursor.getString(4),
                    categoryId = if (cursor.isNull(5)) null else cursor.getLong(5),
                    unit = cursor.getString(6),
                    spec = cursor.getString(7),
                    retailPrice = cursor.getDouble(8),
                    wholesalePrice = cursor.getDouble(9),
                    costPrice = cursor.getDouble(10),
                    supplierCode = cursor.getString(11),
                    createdAt = cursor.getString(12)
                ))
            }
        }
        return list
    }

    fun getByCategory(categoryId: Long): List<Product> {
        val list = mutableListOf<Product>()
        db.rawQuery("SELECT * FROM products WHERE category_id = ? ORDER BY code", arrayOf(categoryId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(Product(
                    id = cursor.getLong(0), code = cursor.getString(1), barcode = cursor.getString(2),
                    name = cursor.getString(3), pinyinCode = cursor.getString(4),
                    categoryId = if (cursor.isNull(5)) null else cursor.getLong(5),
                    unit = cursor.getString(6), spec = cursor.getString(7),
                    retailPrice = cursor.getDouble(8), wholesalePrice = cursor.getDouble(9),
                    costPrice = cursor.getDouble(10), supplierCode = cursor.getString(11),
                    createdAt = cursor.getString(12)
                ))
            }
        }
        return list
    }

    fun getById(id: Long): Product? {
        db.rawQuery("SELECT * FROM products WHERE id = ?", arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
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
        return null
    }

    fun getCategories(): List<Category> {
        val list = mutableListOf<Category>()
        db.rawQuery("SELECT * FROM categories ORDER BY id", null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(Category(
                    id = cursor.getLong(0),
                    name = cursor.getString(1),
                    parentId = if (cursor.isNull(2)) null else cursor.getLong(2),
                    createdAt = cursor.getString(3)
                ))
            }
        }
        return list
    }
}
