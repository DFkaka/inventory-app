package com.example.inventory.ui.sales

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.Customer
import com.example.inventory.data.local.model.Product
import com.example.inventory.data.local.model.SalesOrder
import com.example.inventory.data.repository.CustomerRepository
import com.example.inventory.data.repository.ProductRepository
import com.example.inventory.data.repository.SalesRepository
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SalesListUiState(
    val orders: List<SalesOrder> = emptyList(),
    val keyword: String = "",
    val status: String = "",
    val dateFrom: String = "",
    val dateTo: String = "",
    val isLoading: Boolean = true,
    val allCustomers: List<Customer> = emptyList(),
    val allProducts: List<Product> = emptyList()
)

class SalesListViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SalesRepository(application)
    private val customerRepo = CustomerRepository(application)
    private val productRepo = ProductRepository(application)

    private val _uiState = MutableStateFlow(SalesListUiState())
    val uiState: StateFlow<SalesListUiState> = _uiState

    init {
        loadOrders()
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val customers = customerRepo.getAllCustomers()
                val products = productRepo.searchProducts("")
                _uiState.update { it.copy(allCustomers = customers, allProducts = products) }
            } catch (_: Exception) { }
        }
    }

    fun loadOrders(keyword: String = "", status: String = "", dateFrom: String = "", dateTo: String = "") {
        _uiState.update { it.copy(keyword = keyword, status = status, dateFrom = dateFrom, dateTo = dateTo, isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val orders = repo.getAllOrders(keyword, status, dateFrom, dateTo)
                _uiState.update { it.copy(orders = orders, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun search(keyword: String) {
        loadOrders(keyword, _uiState.value.status, _uiState.value.dateFrom, _uiState.value.dateTo)
    }

    fun filter(status: String) {
        loadOrders(_uiState.value.keyword, status, _uiState.value.dateFrom, _uiState.value.dateTo)
    }

    fun filterDate(dateFrom: String, dateTo: String) {
        loadOrders(_uiState.value.keyword, _uiState.value.status, dateFrom, dateTo)
    }

    fun refreshDropdownData() { loadDropdownData() }
}