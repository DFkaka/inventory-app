package com.example.inventory.ui.customer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.Customer
import com.example.inventory.data.repository.CustomerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerListUiState(
    val customers: List<Customer> = emptyList(),
    val keyword: String = "",
    val isLoading: Boolean = true
)

class CustomerListViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = CustomerRepository(application)

    private val _uiState = MutableStateFlow(CustomerListUiState())
    val uiState: StateFlow<CustomerListUiState> = _uiState

    init {
        loadCustomers()
    }

    fun loadCustomers(keyword: String = "") {
        _uiState.update { it.copy(keyword = keyword, isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val customers = repo.getAllCustomers(keyword)
                _uiState.update { it.copy(customers = customers, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun search(keyword: String) {
        loadCustomers(keyword)
    }
}
