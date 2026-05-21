package com.example.inventory.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.Supplier

class SupplierDao(private val db: SQLiteDatabase) {

    fun getAll(keyword: String = ""): List<Supplier> {
        val list = mutableListOf<Supplier>()
        val sql = if (keyword.isNotBlank())
            "SELECT * FROM suppliers WHERE code LIKE ? OR name LIKE ? ORDER BY code"
        else
            "SELECT * FROM suppliers ORDER BY code"
        val args = if (keyword.isNotBlank()) {
            val like = "%$keyword%"
            arrayOf(like, like)
        } else null

        db.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(Supplier(
                    id = cursor.getLong(0), code = cursor.getString(1),
                    name = cursor.getString(2), contact = cursor.getString(3),
                    bankAccount = cursor.getString(4), note = cursor.getString(5),
                    createdAt = cursor.getString(6)
                ))
            }
        }
        return list
    }

    fun getById(id: Long): Supplier? {
        db.rawQuery("SELECT * FROM suppliers WHERE id = ?", arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                return Supplier(
                    id = cursor.getLong(0), code = cursor.getString(1),
                    name = cursor.getString(2), contact = cursor.getString(3),
                    bankAccount = cursor.getString(4), note = cursor.getString(5),
                    createdAt = cursor.getString(6)
                )
            }
        }
        return null
    }

    fun insert(code: String, name: String, contact: String = "", bankAccount: String = "", note: String = ""): Long {
        val cv = ContentValues().apply {
            put("code", code)
            put("name", name)
            put("contact", contact)
            put("bank_account", bankAccount)
            put("note", note)
        }
        return db.insert("suppliers", null, cv)
    }

    fun delete(id: Long): Int {
        return db.delete("suppliers", "id = ?", arrayOf(id.toString()))
    }

    fun isCodeExists(code: String): Boolean {
        db.rawQuery("SELECT 1 FROM suppliers WHERE code = ?", arrayOf(code)).use { cursor ->
            return cursor.moveToFirst()
        }
    }
}
