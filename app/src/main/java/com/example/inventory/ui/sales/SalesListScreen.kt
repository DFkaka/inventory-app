package com.example.inventory.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.data.local.model.SalesOrder
import com.example.inventory.data.repository.CustomerRepository
import com.example.inventory.data.repository.ProductRepository
import com.example.inventory.data.repository.SalesRepository
import com.example.inventory.ui.component.SearchBar
import com.example.inventory.ui.component.SearchableDropdown
import com.example.inventory.ui.purchase.StatusBadge
import com.example.inventory.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun SalesListScreen(
    onOrderClick: (Long) -> Unit = {},
    onManageCustomer: () -> Unit = {},
    viewModel: SalesListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") }
    var dateFrom by remember { mutableStateOf("") }
    var dateTo by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchBar(
                query = searchText,
                onQueryChange = { searchText = it; viewModel.search(it) },
                placeholder = "搜索单号/客户名称/编码"
            )

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("" to "全部", "已审核" to "已审核", "草稿" to "草稿").forEach { (value, label) ->
                    FilterChip(selected = selectedStatus == value, onClick = { selectedStatus = value; viewModel.filter(value) }, label = { Text(label, fontSize = 12.sp) })
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showDatePicker = true }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (dateFrom.isNotBlank()) "$dateFrom~$dateTo" else "日期", fontSize = 12.sp)
                }
                IconButton(onClick = onManageCustomer, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.People, "客户管理", modifier = Modifier.size(18.dp), tint = Teal500)
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无销售单", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                Text("共 ${uiState.orders.size} 条", fontSize = 12.sp, color = Grey600, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.orders, key = { it.id }) { order -> SalesOrderCard(order, onClick = { onOrderClick(order.id) }) }
                }
            }
        }

        FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            Icon(Icons.Default.Add, "新增销售单")
        }
    }

    if (showAddDialog) { SalesEntryDialog(onDismiss = { showAddDialog = false }, onSaved = { viewModel.loadOrders() }) }

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false }, title = { Text("日期范围") },
            text = {
                Column {
                    OutlinedTextField(dateFrom, { dateFrom = it }, label = { Text("开始") }, placeholder = { Text("2025-01-01") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(dateTo, { dateTo = it }, label = { Text("结束") }, placeholder = { Text("2025-12-31") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.filterDate(dateFrom, dateTo); showDatePicker = false }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { dateFrom = ""; dateTo = ""; viewModel.filterDate("", ""); showDatePicker = false }) { Text("清除") } }
        )
    }
}

@Composable
fun SalesOrderCard(order: SalesOrder, onClick: () -> Unit = {}) {
    Card(onClick = onClick, shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(order.orderNo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                StatusBadge(label = when (order.status) { "draft" -> "草稿"; "shipped" -> "已审核"; "cancelled" -> "已取消"; else -> order.status }, color = when (order.status) { "shipped" -> Green500; "cancelled" -> Red500; else -> Grey600 })
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("客户: ${order.customer}", fontSize = 13.sp)
                Text(order.orderDate, fontSize = 12.sp, color = Grey600)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("¥%.2f".format(order.totalAmount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Green500)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("已付: ¥%.2f".format(order.paidAmount), fontSize = 12.sp, color = Grey600)
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(label = order.paymentStatus, color = if (order.paymentStatus == "已结单") Green500 else Orange500)
                }
            }
        }
    }
}

@Composable
fun SalesEntryDialog(onDismiss: () -> Unit, onSaved: () -> Unit) {
    var customerQuery by remember { mutableStateOf("") }
    var customerOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedCustomer by remember { mutableStateOf("") }
    var orderDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var note by remember { mutableStateOf("") }
    var productQuery by remember { mutableStateOf("") }
    var productOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedProduct by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(customerQuery) {
        scope.launch(Dispatchers.IO) {
            val customers = CustomerRepository(context).getAllCustomers(customerQuery)
            customerOptions = customers.map { "${it.code} | ${it.name}" }
        }
    }

    LaunchedEffect(productQuery) {
        scope.launch(Dispatchers.IO) {
            val products = ProductRepository(context).searchProducts(productQuery)
            productOptions = products.map { "${it.code} | ${it.name}" }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("新增销售单") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                SearchableDropdown(
                    label = "客户 *",
                    query = customerQuery,
                    onQueryChange = { customerQuery = it },
                    options = customerOptions,
                    onOptionSelected = { selectedCustomer = it; customerQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(orderDate, { orderDate = it }, label = { Text("日期") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(note, { note = it }, label = { Text("备注") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("添加商品", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                SearchableDropdown(
                    label = "商品 *",
                    query = productQuery,
                    onQueryChange = { productQuery = it },
                    options = productOptions,
                    onOptionSelected = { selectedProduct = it; productQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(quantity, { quantity = it }, label = { Text("数量 *") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(unitPrice, { unitPrice = it }, label = { Text("单价") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(enabled = selectedCustomer.isNotBlank() && selectedProduct.isNotBlank() && quantity.isNotBlank() && !isSaving, onClick = {
                isSaving = true
                scope.launch(Dispatchers.IO) {
                    val salesRepo = SalesRepository(context)
                    val customerName = selectedCustomer.substringAfter("| ").trim()
                    val orderNo = "SO-${LocalDate.now().toString().replace("-", "")}-${System.currentTimeMillis() % 100000}"
                    val qty = quantity.toDoubleOrNull() ?: 1.0
                    val price = unitPrice.toDoubleOrNull() ?: 0.0
                    val total = qty * price
                    salesRepo.insert(orderNo, customerName, orderDate, total, "draft", note)
                    scope.launch(Dispatchers.Main) { onSaved(); onDismiss() }
                }
            }) { Text(if (isSaving) "保存中..." else "保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
