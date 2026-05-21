package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.SupplierDao
import com.example.inventory.data.local.model.Supplier

class SupplierRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { SupplierDao(db) }

    fun getAllSuppliers(keyword: String = ""): List<Supplier> = dao.getAll(keyword)

    fun getSupplierById(id: Long): Supplier? = dao.getById(id)
}
