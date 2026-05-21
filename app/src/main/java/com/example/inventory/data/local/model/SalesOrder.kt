package com.example.inventory.data.local.model

data class SalesOrder(
    val id: Long = 0,
    val orderNo: String = "",
    val customer: String = "",
    val orderDate: String = "",
    val status: String = "draft",
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentStatus: String = "未结完",
    val note: String = "",
    val createdAt: String = ""
)
