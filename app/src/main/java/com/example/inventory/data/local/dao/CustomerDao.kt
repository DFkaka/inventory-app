package com.example.inventory.data.local.dao

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
}
