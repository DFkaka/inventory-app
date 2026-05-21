package com.example.inventory.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.data.local.model.InventorySummary
import com.example.inventory.ui.component.SearchBar
import com.example.inventory.ui.theme.*

@Composable
fun InventoryScreen(
    onProductClick: (Long) -> Unit = {},
    viewModel: InventoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchText,
            onQueryChange = { searchText = it; viewModel.search(it) },
            placeholder = "搜索商品编码/名称/条码"
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.items, key = { it.productId }) { item ->
                    InventoryItemCard(item = item, onClick = { onProductClick(item.productId) })
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(item: InventorySummary, onClick: () -> Unit) {
    val isLowStock = item.quantity <= item.safetyStock
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (isLowStock) {
                    Text("低库存", fontSize = 11.sp, color = Red500, fontWeight = FontWeight.Bold,
                        modifier = Modifier.background(Red500.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("编码: ${item.code}", fontSize = 12.sp, color = Grey600)
                Text(item.unit, fontSize = 12.sp, color = Grey600)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("库存数量", fontSize = 11.sp, color = Grey600)
                    Text("${item.quantity.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = if (isLowStock) Red500 else Blue700)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("成本/零售", fontSize = 11.sp, color = Grey600)
                    Text("¥%.2f / ¥%.2f".format(item.costPrice, item.retailPrice), fontSize = 13.sp, color = Grey900)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("库存金额", fontSize = 11.sp, color = Grey600)
                    Text("¥%.0f".format(item.totalCost), fontSize = 14.sp,
                        fontWeight = FontWeight.Bold, color = Orange500)
                }
            }
        }
    }
}
