package com.example.inventory.ui.sales_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.inventory.data.local.model.SalesOrderItem
import com.example.inventory.data.repository.ProductRepository
import com.example.inventory.data.repository.SalesRepository
import com.example.inventory.ui.component.SearchableDropdown
import com.example.inventory.ui.purchase.StatusBadge
import com.example.inventory.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesDetailScreen(
    orderId: Long,
    onBack: () -> Unit,
    viewModel: SalesDetailViewModel = viewModel()
) {
    LaunchedEffect(orderId) { viewModel.loadOrder(orderId) }

    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteConfirmId by remember { mutableStateOf<Long?>(null) }
    var editingItem by remember { mutableStateOf<SalesOrderItem?>(null) }

    val isAudited = uiState.order?.status == "shipped"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.order?.orderNo ?: "销售明细", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            if (!isAudited) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "添加明细")
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val order = uiState.order
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order != null) {
                    item {
                        Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("客户: ${order.customer}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    StatusBadge(label = when(order.status) { "draft" -> "草稿"; "shipped" -> "已审核"; "cancelled" -> "已取消"; else -> order.status }, color = when(order.status) { "shipped" -> Green500; "cancelled" -> Red500; else -> Grey600 })
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("日期: ${order.orderDate}", fontSize = 13.sp, color = Grey600)
                                    Text("¥%.2f".format(order.totalAmount), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Green500)
                                }
                                if (order.note.isNotBlank()) Text("备注: ${order.note}", fontSize = 12.sp, color = Grey600, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
 
                if (order != null && order.status == "draft") {
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.approve(order.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                                modifier = Modifier.weight(1f)
                            ) { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("审核") }
                            val isToday = order.orderDate == java.time.LocalDate.now().toString()
                            Button(
                                onClick = { viewModel.reject(order.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Red500),
                                enabled = isToday,
                                modifier = Modifier.weight(1f)
                            ) { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text(if (isToday) "驳回" else "驳回(仅当天)") }
                        }
                    }
                }
 
                item {
                    Text("明细项目 (${uiState.items.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                if (uiState.items.isEmpty()) {
                    item { Text("暂无明细", color = Grey600, fontSize = 13.sp, modifier = Modifier.padding(vertical = 16.dp)) }
                } else {
                    item { SalesItemTableHeader() }
                    items(uiState.items, key = { it.id }) { item ->
                        SalesItemTableRow(item = item, editable = !isAudited, onEdit = { editingItem = item }, onDelete = { deleteConfirmId = item.id })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSalesItemDialog(
            customerName = uiState.order?.customer ?: "",
            onDismiss = { showAddDialog = false },
            onConfirm = { productCode, productName, qty, price, barcode, unit ->
                uiState.order?.let { o -> viewModel.addItem(o.id, productCode, productName, qty, price, barcode, unit) }
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        EditSalesItemDialog(
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { qty, price ->
                uiState.order?.let { o -> viewModel.updateItem(o.id, item.id, qty, price) }
                editingItem = null
            }
        )
    }

    deleteConfirmId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            title = { Text("确认删除") },
            text = { Text("删除此明细项？") },
            confirmButton = {
                TextButton(onClick = {
                    uiState.order?.let { o -> viewModel.deleteItem(o.id, id) }
                    deleteConfirmId = null
                }) { Text("删除", color = Red500) }
            },
            dismissButton = { TextButton(onClick = { deleteConfirmId = null }) { Text("取消") } }
        )
    }
}

@Composable
fun SalesItemCard(item: SalesOrderItem, editable: Boolean = true, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Card(shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${item.productName} [${item.productCode}]", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("单价: ¥%.2f | 数量: %.1f %s".format(item.unitPrice, item.quantity, item.unit), fontSize = 12.sp, color = Grey600)
                Text("小计: ¥%.2f".format(item.subtotal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Green500)
            }
            if (editable) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "编辑", tint = Blue700, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "删除", tint = Red500, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun EditSalesItemDialog(item: SalesOrderItem, onDismiss: () -> Unit, onConfirm: (Double, Double) -> Unit) {
    var quantity by remember { mutableStateOf(item.quantity.toString()) }
    var unitPrice by remember { mutableStateOf(item.unitPrice.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("编辑明细") },
        text = {
            Column {
                Text("${item.productName} [${item.productCode}]", fontSize = 13.sp, color = Grey600)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(quantity, { quantity = it }, label = { Text("数量 *") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(unitPrice, { unitPrice = it }, label = { Text("单价") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(enabled = quantity.isNotBlank(), onClick = {
                onConfirm(quantity.toDoubleOrNull() ?: 1.0, unitPrice.toDoubleOrNull() ?: 0.0)
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun AddSalesItemDialog(customerName: String = "", onDismiss: () -> Unit, onConfirm: (productCode: String, productName: String, qty: Double, price: Double, barcode: String, unit: String) -> Unit) {
    var productQuery by remember { mutableStateOf("") }
    var productOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedProduct by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(selectedProduct) {
        if (selectedProduct.isNotBlank() && customerName.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
                val code = selectedProduct.substringBefore(" |")
                val lastPrice = SalesRepository(context).getLastPrice(customerName, code)
                if (lastPrice != null && lastPrice > 0) {
                    unitPrice = String.format("%.2f", lastPrice)
                }
            }
        }
    }

    LaunchedEffect(productQuery) {
        scope.launch(Dispatchers.IO) {
            val products = ProductRepository(context).searchProducts(productQuery)
            productOptions = products.map { "${it.code} | ${it.name} | ${it.unit}" }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("添加明细") },
        text = {
            Column {
                SearchableDropdown(
                    label = "商品 *", query = productQuery,
                    onQueryChange = { productQuery = it },
                    options = productOptions,
                    onOptionSelected = { selectedProduct = it; productQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(quantity, { quantity = it }, label = { Text("数量 *") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(unitPrice, { unitPrice = it }, label = { Text("单价") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(enabled = selectedProduct.isNotBlank() && quantity.isNotBlank(), onClick = {
                val parts = selectedProduct.split(" | ")
                val code = parts.getOrNull(0) ?: ""
                val name = parts.getOrNull(1) ?: ""
                val unit = parts.getOrNull(2) ?: "个"
                onConfirm(code, name, quantity.toDoubleOrNull() ?: 1.0, unitPrice.toDoubleOrNull() ?: 0.0, "", unit)
            }) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun SalesItemTableHeader() {
    Surface(color = Blue50) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text("商品编码", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.width(72.dp))
            Text("名称", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.weight(1f))
            Text("数量", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.width(48.dp))
            Text("单价", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.width(64.dp))
            Text("小计", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.width(72.dp))
            Text("操作", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.width(72.dp))
        }
    }
    HorizontalDivider(thickness = 1.dp, color = Grey400)
}

@Composable
fun SalesItemTableRow(item: SalesOrderItem, editable: Boolean = true, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(item.productCode, fontSize = 11.sp, modifier = Modifier.width(72.dp))
        Text(item.productName, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
        Text("%.1f".format(item.quantity), fontSize = 11.sp, modifier = Modifier.width(48.dp))
        Text("¥%.2f".format(item.unitPrice), fontSize = 11.sp, modifier = Modifier.width(64.dp))
        Text("¥%.2f".format(item.subtotal), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Green500, modifier = Modifier.width(72.dp))
        if (editable) {
            Row(modifier = Modifier.width(72.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, "编辑", tint = Blue700, modifier = Modifier.size(16.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, "删除", tint = Red500, modifier = Modifier.size(16.dp)) }
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Grey200)
}

@Composable
fun SalesItemTableHeader() {
    Surface(color = Blue50) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text("编码", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.width(72.dp))
            Text("名称", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.weight(1f))
            Text("数量", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.width(48.dp))
            Text("单价", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.width(64.dp))
            Text("小计", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.width(72.dp))
            Text("操作", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey900, modifier = Modifier.width(72.dp))
        }
    }
    HorizontalDivider(thickness = 1.dp, color = Grey400)
}

@Composable
fun SalesItemTableRow(item: SalesOrderItem, editable: Boolean = true, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(item.productCode, fontSize = 11.sp, modifier = Modifier.width(72.dp))
        Text(item.productName, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
        Text("%.1f".format(item.quantity), fontSize = 11.sp, modifier = Modifier.width(48.dp))
        Text("¥%.2f".format(item.unitPrice), fontSize = 11.sp, modifier = Modifier.width(64.dp))
        Text("¥%.2f".format(item.subtotal), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Green500, modifier = Modifier.width(72.dp))
        if (editable) {
            Row(modifier = Modifier.width(72.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, "编辑", tint = Blue700, modifier = Modifier.size(16.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, "删除", tint = Red500, modifier = Modifier.size(16.dp)) }
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Grey200)
}