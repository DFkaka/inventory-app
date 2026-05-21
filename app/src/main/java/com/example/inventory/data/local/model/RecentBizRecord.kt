package com.example.inventory.data.local.model

data class RecentBizRecord(
    val type: String,
    val orderNo: String,
    val partyName: String,
    val orderDate: String,
    val totalAmount: Double,
    val status: String
)
