package com.example.inventory.ui.sales_detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.SalesOrder
import com.example.inventory.data.local.model.SalesOrderItem
import com.example.inventory.data.repository.ProductRepository
import com.example.inventory.data.repository.SalesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SalesDetailUiState(
    val order: SalesOrder? = null,
    val items: List<SalesOrderItem> = emptyList(),
    val isLoading: Boolean = true
)

class SalesDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val salesRepo = SalesRepository(application)
    private val productRepo = ProductRepository(application)

    private val _uiState = MutableStateFlow(SalesDetailUiState())
    val uiState: StateFlow<SalesDetailUiState> = _uiState

    fun loadOrder(orderId: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val order = salesRepo.getOrderById(orderId)
                val items = salesRepo.getItems(orderId)
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
            salesRepo.insertItem(orderId, productId, quantity, unitPrice, barcode, unit)
            val items = salesRepo.getItems(orderId)
            val total = items.sumOf { it.subtotal }
            salesRepo.updateTotalAmount(orderId, total)
            val order = salesRepo.getOrderById(orderId)
            _uiState.update { it.copy(order = order, items = items) }
        }
    }

    fun deleteItem(orderId: Long, itemId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            salesRepo.deleteItem(itemId)
            val items = salesRepo.getItems(orderId)
            val total = items.sumOf { it.subtotal }
            salesRepo.updateTotalAmount(orderId, total)
            val order = salesRepo.getOrderById(orderId)
            _uiState.update { it.copy(order = order, items = items) }
        }
    }
}
