package com.example.inventory.data.local.model

data class InventoryItem(
    val id: Long = 0,
    val productId: Long = 0,
    val quantity: Double = 0.0,
    val safetyStock: Double = 0.0,
    val updatedAt: String = ""
)
