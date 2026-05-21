package com.example.inventory.ui.purchase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.PurchaseOrder
import com.example.inventory.data.repository.PurchaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PurchaseListUiState(
    val orders: List<PurchaseOrder> = emptyList(),
    val keyword: String = "",
    val isLoading: Boolean = true
)

class PurchaseListViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = PurchaseRepository(application)

    private val _uiState = MutableStateFlow(PurchaseListUiState())
    val uiState: StateFlow<PurchaseListUiState> = _uiState

    init {
        loadOrders()
    }

    fun loadOrders(keyword: String = "") {
        _uiState.update { it.copy(keyword = keyword, isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val orders = repo.getAllOrders(keyword)
                _uiState.update { it.copy(orders = orders, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun search(keyword: String) {
        loadOrders(keyword)
    }
}
