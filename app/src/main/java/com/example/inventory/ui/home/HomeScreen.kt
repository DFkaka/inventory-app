package com.example.inventory.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.data.local.model.RecentBizRecord
import com.example.inventory.ui.component.StatCard
import com.example.inventory.ui.theme.*

@Composable
fun HomeScreen(
    onNavigate: (Long) -> Unit = {},
    onOrderClick: (type: String, orderId: Long) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadStats() }

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

            if (uiState.recentRecords.isNotEmpty()) {
                item {
                    Text("最近业务状态", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp))
                }
                items(uiState.recentRecords.take(5)) { record ->
                    RecentBizCard(record, onClick = { onOrderClick(record.type, record.orderId) })
                }
            }
        }
    }
}

@Composable
fun RecentBizCard(record: RecentBizRecord, onClick: () -> Unit = {}) {
    val isPurchase = record.type == "采购"
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = record.type,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPurchase) Orange500 else Green500,
                modifier = Modifier.width(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(record.orderNo, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(if (isPurchase) "供应商: ${record.partyName}" else "客户: ${record.partyName}",
                    fontSize = 12.sp, color = Grey600)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("¥%.2f".format(record.totalAmount),
                    fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = if (isPurchase) Orange500 else Green500)
                Text(record.orderDate, fontSize = 11.sp, color = Grey600)
                Text(when (record.status) {
                    "draft" -> "草稿"
                    "received", "shipped" -> "已审核"
                    "cancelled" -> "已取消"
                    else -> record.status
                }, fontSize = 11.sp, color = Grey600)
            }
        }
    }
}