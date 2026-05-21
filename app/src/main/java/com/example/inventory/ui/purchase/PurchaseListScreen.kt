package com.example.inventory.ui.purchase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.data.local.model.PurchaseOrder
import com.example.inventory.ui.component.SearchBar
import com.example.inventory.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseListScreen(
    onBack: () -> Unit,
    viewModel: PurchaseListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("进货单", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchBar(
                query = searchText,
                onQueryChange = {
                    searchText = it
                    viewModel.search(it)
                },
                placeholder = "搜索单号/供应商"
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无进货单", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        OrderCard(order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: PurchaseOrder) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.orderNo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                StatusChip(
                    label = when (order.status) {
                        "draft" -> "草稿"
                        "received" -> "已收货"
                        "cancelled" -> "已取消"
                        else -> order.status
                    },
                    color = when (order.status) {
                        "received" -> Green500
                        "cancelled" -> Red500
                        else -> Grey600
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("供应商: ${order.supplier}", fontSize = 13.sp)
                Text(order.orderDate, fontSize = 12.sp, color = Grey600)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¥%.2f".format(order.totalAmount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange500
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("已付: ¥%.2f".format(order.paidAmount), fontSize = 12.sp, color = Grey600)
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusChip(
                        label = order.paymentStatus,
                        color = if (order.paymentStatus == "已结单") Green500 else Orange500
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(label: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
