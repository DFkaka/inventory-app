package com.example.inventory.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.data.local.model.SalesOrder
import com.example.inventory.ui.component.SearchBar
import com.example.inventory.ui.purchase.StatusBadge
import com.example.inventory.ui.theme.*

@Composable
fun SalesListScreen(
    viewModel: SalesListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") }
    var dateFrom by remember { mutableStateOf("") }
    var dateTo by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchText,
            onQueryChange = {
                searchText = it
                viewModel.search(it)
            },
            placeholder = "搜索单号/客户名称/编码"
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("" to "全部", "已审核" to "已审核", "草稿" to "草稿").forEach { (value, label) ->
                FilterChip(
                    selected = selectedStatus == value,
                    onClick = {
                        selectedStatus = value
                        viewModel.filter(value)
                    },
                    label = { Text(label, fontSize = 12.sp) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = { showDatePicker = true },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (dateFrom.isNotBlank()) "$dateFrom~$dateTo" else "日期",
                    fontSize = 12.sp
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无销售单", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Text("共 ${uiState.orders.size} 条", fontSize = 12.sp, color = Grey600,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.orders, key = { it.id }) { order ->
                    SalesOrderCard(order)
                }
            }
        }
    }

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("日期范围") },
            text = {
                Column {
                    OutlinedTextField(
                        value = dateFrom,
                        onValueChange = { dateFrom = it },
                        label = { Text("开始日期") },
                        placeholder = { Text("2025-01-01") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dateTo,
                        onValueChange = { dateTo = it },
                        label = { Text("结束日期") },
                        placeholder = { Text("2025-12-31") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.filterDate(dateFrom, dateTo)
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    dateFrom = ""
                    dateTo = ""
                    viewModel.filterDate("", "")
                    showDatePicker = false
                }) { Text("清除") }
            }
        )
    }
}

@Composable
fun SalesOrderCard(order: SalesOrder) {
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
                StatusBadge(
                    label = when (order.status) {
                        "draft" -> "草稿"; "shipped" -> "已审核"; "cancelled" -> "已取消"; else -> order.status
                    },
                    color = when (order.status) {
                        "shipped" -> Green500; "cancelled" -> Red500; else -> Grey600
                    }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("客户: ${order.customer}", fontSize = 13.sp)
                Text(order.orderDate, fontSize = 12.sp, color = Grey600)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¥%.2f".format(order.totalAmount), fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, color = Green500)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("已付: ¥%.2f".format(order.paidAmount), fontSize = 12.sp, color = Grey600)
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(
                        label = order.paymentStatus,
                        color = if (order.paymentStatus == "已结单") Green500 else Orange500
                    )
                }
            }
        }
    }
}
