package com.example.inventory.ui.purchase_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.inventory.data.local.model.PurchaseOrderItem
import com.example.inventory.data.repository.ProductRepository
import com.example.inventory.ui.component.SearchableDropdown
import com.example.inventory.ui.purchase.StatusBadge
import com.example.inventory.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDetailScreen(
    orderId: Long,
    onBack: () -> Unit,
    viewModel: PurchaseDetailViewModel = viewModel()
) {
    LaunchedEffect(orderId) { viewModel.loadOrder(orderId) }

    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteConfirmId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.order?.orderNo ?: "进货明细", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "添加明细")
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
                                    Text("供应商: ${order.supplier}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    StatusBadge(label = when(order.status) { "draft" -> "草稿"; "received" -> "已审核"; "cancelled" -> "已取消"; else -> order.status }, color = when(order.status) { "received" -> Green500; "cancelled" -> Red500; else -> Grey600 })
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("日期: ${order.orderDate}", fontSize = 13.sp, color = Grey600)
                                    Text("¥%.2f".format(order.totalAmount), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Orange500)
                                }
                                if (order.note.isNotBlank()) Text("备注: ${order.note}", fontSize = 12.sp, color = Grey600, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }

                item {
                    Text("明细项目 (${uiState.items.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                if (uiState.items.isEmpty()) {
                    item { Text("暂无明细", color = Grey600, fontSize = 13.sp, modifier = Modifier.padding(vertical = 16.dp)) }
                } else {
                    items(uiState.items, key = { it.id }) { item ->
                        ItemCard(item = item, onDelete = { deleteConfirmId = item.id })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { productCode, productName, qty, price, barcode, unit ->
                uiState.order?.let { o ->
                    viewModel.addItem(o.id, productCode, productName, qty, price, barcode, unit)
                }
                showAddDialog = false
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
fun ItemCard(item: PurchaseOrderItem, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("商品ID: ${item.productId}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("单价: ¥%.2f | 数量: ${item.quantity.toInt()} ${item.unit}".format(item.unitPrice), fontSize = 12.sp, color = Grey600)
                Text("小计: ¥%.2f".format(item.subtotal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Orange500)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "删除", tint = Red500, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onConfirm: (productCode: String, productName: String, qty: Double, price: Double, barcode: String, unit: String) -> Unit) {
    var productQuery by remember { mutableStateOf("") }
    var productOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedProduct by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
