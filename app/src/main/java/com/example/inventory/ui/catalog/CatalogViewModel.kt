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
    val isLoading: Boolean = true
)

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ProductRepository(application)

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState

    init {
        loadProducts()
    }

    fun loadProducts(keyword: String = "") {
        _uiState.update { it.copy(keyword = keyword, isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val products = if (keyword.isBlank()) repo.getAllProducts() else repo.searchProducts(keyword)
                _uiState.update { it.copy(products = products, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun search(keyword: String) {
        loadProducts(keyword)
    }
}
