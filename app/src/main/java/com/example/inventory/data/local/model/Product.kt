package com.example.inventory.data.local.model

data class Product(
    val id: Long = 0,
    val code: String = "",
    val barcode: String = "",
    val name: String = "",
    val pinyinCode: String = "",
    val categoryId: Long? = null,
    val unit: String = "个",
    val spec: String = "",
    val retailPrice: Double = 0.0,
    val wholesalePrice: Double = 0.0,
    val costPrice: Double = 0.0,
    val supplierCode: String = "",
    val createdAt: String = ""
)
