package com.example.inventory.ui.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.local.model.InventorySummary
import com.example.inventory.data.repository.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventoryUiState(
    val items: List<InventorySummary> = emptyList(),
    val keyword: String = "",
    val isLoading: Boolean = true
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = InventoryRepository(application)

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState

    init {
        loadInventory()
    }

    fun loadInventory(keyword: String = "") {
        _uiState.update { it.copy(keyword = keyword, isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = repo.getInventorySummary(keyword)
                _uiState.update { it.copy(items = items, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun search(keyword: String) {
        loadInventory(keyword)
    }
}
