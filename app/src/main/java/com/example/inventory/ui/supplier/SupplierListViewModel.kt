package com.example.inventory.ui.supplier

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.Supplier
import com.example.inventory.data.repository.SupplierRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupplierListUiState(
    val suppliers: List<Supplier> = emptyList(),
    val keyword: String = "",
    val isLoading: Boolean = true
)

class SupplierListViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SupplierRepository(application)

    private val _uiState = MutableStateFlow(SupplierListUiState())
    val uiState: StateFlow<SupplierListUiState> = _uiState

    init {
        loadSuppliers()
    }

    fun loadSuppliers(keyword: String = "") {
        _uiState.update { it.copy(keyword = keyword, isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val suppliers = repo.getAllSuppliers(keyword)
                _uiState.update { it.copy(suppliers = suppliers, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun search(keyword: String) {
        loadSuppliers(keyword)
    }
}
