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

data class SupplierManageUiState(
    val suppliers: List<Supplier> = emptyList(),
    val keyword: String = "",
    val isLoading: Boolean = true
)

class SupplierManageViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SupplierRepository(application)
    private val _uiState = MutableStateFlow(SupplierManageUiState())
    val uiState: StateFlow<SupplierManageUiState> = _uiState

    init { loadSuppliers() }

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

    fun search(keyword: String) { loadSuppliers(keyword) }

    fun insert(code: String, name: String, contact: String, bank: String, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.insert(code, name, contact, bank, note)
            loadSuppliers(_uiState.value.keyword)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.delete(id)
            loadSuppliers(_uiState.value.keyword)
        }
    }
    fun update(id: Long, name: String, contact: String, bank: String, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.update(id, name, contact, bank, note)
            loadSuppliers(_uiState.value.keyword)
        }
    }
}
