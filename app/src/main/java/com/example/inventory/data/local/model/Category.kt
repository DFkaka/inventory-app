package com.example.inventory.data.local.model

data class Category(
    val id: Long = 0,
    val name: String = "",
    val parentId: Long? = null,
    val createdAt: String = ""
)
