package com.example.inventory.ui.product

import androidx.compose.foundation.background
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
import com.example.inventory.data.local.model.InventoryLog
import com.example.inventory.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    onBack: () -> Unit,
    viewModel: ProductDetailViewModel = viewModel()
) {
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.product?.name ?: "商品详情", fontWeight = FontWeight.Bold) },
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
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val product = uiState.product
            if (product == null) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("商品不存在", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Basic info card
                    item {
                        Card(shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("基本信息", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoRow("商品编码", product.code)
                                InfoRow("条码", product.barcode)
                                InfoRow("分类", product.categoryName)
                                InfoRow("单位", product.unit)
                                InfoRow("规格", product.spec)
                            }
                        }
                    }

                    // Price info
                    item {
                        Card(shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("价格信息", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoRow("成本价", "¥%.2f".format(product.costPrice))
                                InfoRow("批发价", "¥%.2f".format(product.wholesalePrice))
                                InfoRow("零售价", "¥%.2f".format(product.retailPrice))
                            }
                        }
                    }

                    // Inventory info
                    item {
                        val isLowStock = product.quantity <= product.safetyStock
                        Card(shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("库存信息", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${product.quantity.toInt()}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLowStock) Red500 else Blue700
                                        )
                                        Text("当前库存", fontSize = 12.sp, color = Grey600)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${product.safetyStock.toInt()}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Orange500
                                        )
                                        Text("安全库存", fontSize = 12.sp, color = Grey600)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "¥%.0f".format(product.totalCost),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Teal500
                                        )
                                        Text("库存金额", fontSize = 12.sp, color = Grey600)
                                    }
                                }
                                if (isLowStock) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("⚠ 库存低于安全线", color = Red500, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Inventory logs
                    if (uiState.logs.isNotEmpty()) {
                        item {
                            Text("库存流水", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                modifier = Modifier.padding(top = 8.dp))
                        }
                        items(uiState.logs) { log ->
                            LogItem(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Grey600)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LogItem(log: InventoryLog) {
    val isIn = log.delta > 0
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isIn) "+${log.delta.toInt()}" else "${log.delta.toInt()}",
                color = if (isIn) Green500 else Red500,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (log.remark != null) {
                    Text(log.remark, fontSize = 13.sp)
                }
                Text("${log.beforeQty.toInt()} → ${log.afterQty.toInt()}", fontSize = 11.sp, color = Grey600)
            }
            if (log.createdAt != null) {
                Text(log.createdAt.substringAfterLast(" "), fontSize = 11.sp, color = Grey600)
            }
        }
    }
}
