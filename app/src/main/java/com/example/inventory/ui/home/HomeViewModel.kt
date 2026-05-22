package com.example.inventory.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.RecentBizRecord
import com.example.inventory.data.repository.InventoryRepository
import com.example.inventory.data.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val productCount: Int = 0,
    val totalCost: Double = 0.0,
    val totalRetail: Double = 0.0,
    val purchaseTotal: Double = 0.0,
    val salesTotal: Double = 0.0,
    val lowStockCount: Int = 0,
    val recentRecords: List<RecentBizRecord> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val inventoryRepo = InventoryRepository(application)
    private val reportRepo = ReportRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (productCount, totalCost, totalRetail) = inventoryRepo.getTotalStats()
                val purchaseTotal = reportRepo.getPurchaseTotal()
                val salesTotal = reportRepo.getSalesTotal()
                val lowStockCount = inventoryRepo.getLowStockAlerts().size
                val recentRecords = reportRepo.getRecentBizRecords(10)

                _uiState.value = HomeUiState(
                    productCount = productCount,
                    totalCost = totalCost,
                    totalRetail = totalRetail,
                    purchaseTotal = purchaseTotal,
                    salesTotal = salesTotal,
                    lowStockCount = lowStockCount,
                    recentRecords = recentRecords,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
