package com.example.inventory.data.local.model

data class InventoryLog(
    val id: Long = 0,
    val productId: Long = 0,
    val delta: Double = 0.0,
    val beforeQty: Double = 0.0,
    val afterQty: Double = 0.0,
    val remark: String? = null,
    val createdAt: String? = null
)
