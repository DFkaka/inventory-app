package com.example.inventory.data.local.model

data class PurchaseOrderItem(
    val id: Long = 0,
    val orderId: Long = 0,
    val productId: Long = 0,
    val productName: String = "",
    val productCode: String = "",
    val quantity: Double = 0.0,
    val unitPrice: Double = 0.0,
    val subtotal: Double = 0.0,
    val barcode: String = "",
    val unit: String = "个",
    val spec: String = ""
)