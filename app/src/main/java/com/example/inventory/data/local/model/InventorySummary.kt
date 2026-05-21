package com.example.inventory.data.local.model

data class InventorySummary(
    val productId: Long = 0,
    val code: String = "",
    val name: String = "",
    val barcode: String = "",
    val categoryName: String = "",
    val unit: String = "",
    val spec: String = "",
    val quantity: Double = 0.0,
    val safetyStock: Double = 0.0,
    val costPrice: Double = 0.0,
    val retailPrice: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalRetail: Double = 0.0
)
