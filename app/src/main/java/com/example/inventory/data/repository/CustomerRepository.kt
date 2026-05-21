package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.CustomerDao
import com.example.inventory.data.local.model.Customer

class CustomerRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { CustomerDao(db) }

    fun getAllCustomers(keyword: String = ""): List<Customer> = dao.getAll(keyword)

    fun getCustomerById(id: Long): Customer? = dao.getById(id)
}
