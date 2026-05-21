package com.example.inventory.data.local.model

data class Customer(
    val id: Long = 0,
    val code: String = "",
    val name: String = "",
    val contact: String = "",
    val phone: String = "",
    val address: String = "",
    val note: String = "",
    val createdAt: String = ""
)
