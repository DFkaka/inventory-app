package com.example.inventory.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.inventory.data.local.model.Customer

class CustomerDao(private val db: SQLiteDatabase) {

    fun getAll(keyword: String = ""): List<Customer> {
        val list = mutableListOf<Customer>()
        val sql = if (keyword.isNotBlank())
            "SELECT * FROM customers WHERE code LIKE ? OR name LIKE ? ORDER BY code"
        else
            "SELECT * FROM customers ORDER BY code"
        val args = if (keyword.isNotBlank()) {
            val like = "%$keyword%"
            arrayOf(like, like)
        } else null

        db.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(Customer(
                    id = cursor.getLong(0), code = cursor.getString(1),
                    name = cursor.getString(2), contact = cursor.getString(3),
                    phone = cursor.getString(4), address = cursor.getString(5),
                    note = cursor.getString(6), createdAt = cursor.getString(7)
                ))
            }
        }
        return list
    }

    fun getById(id: Long): Customer? {
        db.rawQuery("SELECT * FROM customers WHERE id = ?", arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                return Customer(
                    id = cursor.getLong(0), code = cursor.getString(1),
                    name = cursor.getString(2), contact = cursor.getString(3),
                    phone = cursor.getString(4), address = cursor.getString(5),
                    note = cursor.getString(6), createdAt = cursor.getString(7)
                )
            }
        }
        return null
    }

    fun insert(code: String, name: String, contact: String = "", phone: String = "", address: String = "", note: String = ""): Long {
        val cv = ContentValues().apply {
            put("code", code)
            put("name", name)
            put("contact", contact)
            put("phone", phone)
            put("address", address)
            put("note", note)
        }
        return db.insert("customers", null, cv)
    }

    fun delete(id: Long): Int {
        return db.delete("customers", "id = ?", arrayOf(id.toString()))
    }

    fun isCodeExists(code: String): Boolean {
        return db.rawQuery("SELECT 1 FROM customers WHERE code = ?", arrayOf(code)).use { it.moveToFirst() }
    }
}
