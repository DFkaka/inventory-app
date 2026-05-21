package com.example.inventory.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.ui.component.StatCard
import com.example.inventory.ui.theme.*

@Composable
fun HomeScreen(
    onNavigate: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("商品数", uiState.productCount.toString(), Blue700, Modifier.weight(1f))
                    StatCard("库存成本", "¥%.0f".format(uiState.totalCost), Orange500, Modifier.weight(1f))
                    StatCard("库存市值", "¥%.0f".format(uiState.totalRetail), Green500, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("累计采购", "¥%.0f".format(uiState.purchaseTotal), Blue700, Modifier.weight(1f))
                    StatCard("累计销售", "¥%.0f".format(uiState.salesTotal), Teal500, Modifier.weight(1f))
                    StatCard("库存预警", uiState.lowStockCount.toString(),
                        if (uiState.lowStockCount > 0) Red500 else Green500, Modifier.weight(1f))
                }
            }
        }
    }
}
