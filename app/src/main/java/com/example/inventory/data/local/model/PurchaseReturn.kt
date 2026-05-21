package com.example.inventory.data.local.model

data class PurchaseReturn(
    val id: Long = 0,
    val returnNo: String = "",
    val supplier: String = "",
    val returnDate: String = "",
    val status: String = "draft",
    val totalAmount: Double = 0.0,
    val note: String = "",
    val createdAt: String = ""
)
