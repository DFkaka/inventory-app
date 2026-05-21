package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.SupplierDao
import com.example.inventory.data.local.model.Supplier

class SupplierRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { SupplierDao(db) }

    fun getAllSuppliers(keyword: String = ""): List<Supplier> = dao.getAll(keyword)
    fun getSupplierById(id: Long): Supplier? = dao.getById(id)
    fun generateCode(prefix: String = ""): String = dao.generateCode(prefix)
    fun insert(code: String, name: String, contact: String = "", bankAccount: String = "", note: String = ""): Long =
        dao.insert(code, name, contact, bankAccount, note)
    fun update(id: Long, name: String, contact: String = "", bankAccount: String = "", note: String = "") = dao.update(id, name, contact, bankAccount, note)
    fun delete(id: Long): Int = dao.delete(id)
    fun isCodeExists(code: String): Boolean = dao.isCodeExists(code)
}
