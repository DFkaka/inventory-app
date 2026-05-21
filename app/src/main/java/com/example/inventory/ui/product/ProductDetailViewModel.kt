package com.example.inventory.ui.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.InventoryLog
import com.example.inventory.data.local.model.InventorySummary
import com.example.inventory.data.repository.InventoryRepository
import com.example.inventory.data.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: InventorySummary? = null,
    val logs: List<InventoryLog> = emptyList(),
    val isLoading: Boolean = true
)

class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val inventoryRepo = InventoryRepository(application)
    private val reportRepo = ReportRepository(application)

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState

    fun loadProduct(productId: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val product = inventoryRepo.getInventorySummary().find { it.productId == productId }
                val logs = reportRepo.getInventoryLogs(productId, 50)
                _uiState.update { it.copy(product = product, logs = logs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
