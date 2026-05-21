package com.example.inventory.ui.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.Product
import com.example.inventory.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val products: List<Product> = emptyList(),
    val keyword: String = "",
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false
)

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ProductRepository(application)

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState

    fun search(keyword: String) {
        _uiState.update { it.copy(keyword = keyword, isLoading = true, hasSearched = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val products = if (keyword.isBlank()) emptyList() else repo.searchProducts(keyword)
                _uiState.update { it.copy(products = products, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun reload() {
        if (_uiState.value.keyword.isNotBlank()) {
            search(_uiState.value.keyword)
        }
    }
}
