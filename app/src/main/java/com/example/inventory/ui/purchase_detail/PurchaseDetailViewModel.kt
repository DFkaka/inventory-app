package com.example.inventory.ui.purchase_detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.PurchaseOrder
import com.example.inventory.data.local.model.PurchaseOrderItem
import com.example.inventory.data.repository.ProductRepository
import com.example.inventory.data.repository.PurchaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PurchaseDetailUiState(
    val order: PurchaseOrder? = null,
    val items: List<PurchaseOrderItem> = emptyList(),
    val isLoading: Boolean = true
)

class PurchaseDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val purchaseRepo = PurchaseRepository(application)
    private val productRepo = ProductRepository(application)

    private val _uiState = MutableStateFlow(PurchaseDetailUiState())
    val uiState: StateFlow<PurchaseDetailUiState> = _uiState

    fun loadOrder(orderId: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val order = purchaseRepo.getOrderById(orderId)
                val items = purchaseRepo.getItems(orderId)
                _uiState.update { it.copy(order = order, items = items, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addItem(orderId: Long, productCode: String, productName: String, quantity: Double, unitPrice: Double, barcode: String, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val products = productRepo.searchProducts(productCode.ifBlank { productName })
            val product = products.firstOrNull()
            val productId = product?.id ?: 0L
            purchaseRepo.insertItem(orderId, productId, quantity, unitPrice, barcode, unit)
            val items = purchaseRepo.getItems(orderId)
            val total = items.sumOf { it.subtotal }
            purchaseRepo.updateTotalAmount(orderId, total)
            val order = purchaseRepo.getOrderById(orderId)
            _uiState.update { it.copy(order = order, items = items) }
        }
    }

    fun deleteItem(orderId: Long, itemId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseRepo.deleteItem(itemId)
            val items = purchaseRepo.getItems(orderId)
            val total = items.sumOf { it.subtotal }
            purchaseRepo.updateTotalAmount(orderId, total)
            val order = purchaseRepo.getOrderById(orderId)
            _uiState.update { it.copy(order = order, items = items) }
        }
    }


    fun updateItem(orderId: Long, itemId: Long, quantity: Double, unitPrice: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseRepo.updateItem(itemId, quantity, unitPrice)
            val items = purchaseRepo.getItems(orderId)
            val total = items.sumOf { it.subtotal }
            purchaseRepo.updateTotalAmount(orderId, total)
            val order = purchaseRepo.getOrderById(orderId)
            _uiState.update { it.copy(order = order, items = items) }
        }
    }}
