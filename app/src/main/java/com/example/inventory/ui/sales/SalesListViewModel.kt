package com.example.inventory.ui.sales

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.SalesOrder
import com.example.inventory.data.repository.SalesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SalesListUiState(
    val orders: List<SalesOrder> = emptyList(),
    val keyword: String = "",
    val isLoading: Boolean = true
)

class SalesListViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SalesRepository(application)

    private val _uiState = MutableStateFlow(SalesListUiState())
    val uiState: StateFlow<SalesListUiState> = _uiState

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
