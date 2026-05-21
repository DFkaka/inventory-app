package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.data.local.dao.ProductDao
import com.example.inventory.data.local.model.Category
import com.example.inventory.data.local.model.Product

class ProductRepository(context: Context) : BaseRepository(context) {

    private val dao by lazy { ProductDao(db) }

    fun getAllProducts(): List<Product> = dao.getAll()
    fun searchProducts(keyword: String): List<Product> = dao.search(keyword)
    fun getProductById(id: Long): Product? = dao.getById(id)
    fun getProductsByCategory(categoryId: Long): List<Product> = dao.getByCategory(categoryId)
    fun getAllCategories(): List<Category> = dao.getCategories()
    fun generateCode(prefix: String = ""): String = dao.generateCode(prefix)
    fun insert(code: String, name: String, barcode: String = "", pinyinCode: String = "",
               categoryId: Long? = null, unit: String = "个", spec: String = "",
               retailPrice: Double = 0.0, wholesalePrice: Double = 0.0, costPrice: Double = 0.0,
               supplierCode: String = ""): Long =
        dao.insert(code, name, barcode, pinyinCode, categoryId, unit, spec, retailPrice, wholesalePrice, costPrice, supplierCode)
}
