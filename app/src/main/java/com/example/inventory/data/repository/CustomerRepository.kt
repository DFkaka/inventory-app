package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.CustomerDao
import com.example.inventory.data.local.model.Customer

class CustomerRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { CustomerDao(db) }

    fun getAllCustomers(keyword: String = ""): List<Customer> = dao.getAll(keyword)
    fun getCustomerById(id: Long): Customer? = dao.getById(id)
    fun generateCode(prefix: String = ""): String = dao.generateCode(prefix)
    fun insert(code: String, name: String, contact: String = "", phone: String = "", address: String = "", note: String = ""): Long =
        dao.insert(code, name, contact, phone, address, note)
    fun update(id: Long, name: String, contact: String = "", phone: String = "", address: String = "", note: String = "") = dao.update(id, name, contact, phone, address, note)
    fun delete(id: Long): Int = dao.delete(id)
    fun isCodeExists(code: String): Boolean = dao.isCodeExists(code)
}
