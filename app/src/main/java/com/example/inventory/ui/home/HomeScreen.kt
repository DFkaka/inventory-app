package com.example.inventory.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.ui.component.StatCard
import com.example.inventory.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("进销存查询", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stats cards row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "商品数",
                            value = uiState.productCount.toString(),
                            valueColor = Blue700,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "库存成本",
                            value = "¥%.0f".format(uiState.totalCost),
                            valueColor = Orange500,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "库存市值",
                            value = "¥%.0f".format(uiState.totalRetail),
                            valueColor = Green500,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Second row stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "累计采购",
                            value = "¥%.0f".format(uiState.purchaseTotal),
                            valueColor = Blue700,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "累计销售",
                            value = "¥%.0f".format(uiState.salesTotal),
                            valueColor = Teal500,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "库存预警",
                            value = uiState.lowStockCount.toString(),
                            valueColor = if (uiState.lowStockCount > 0) Red500 else Green500,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Menu items
                item {
                    Text("功能菜单", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp))
                }

                item { MenuCard("库存总览", "查看所有商品库存状态", Icons.Default.Inventory, Blue700) { onNavigate("inventory") } }
                item { MenuCard("进货单", "采购订单查询", Icons.Default.ShoppingCart, Orange500) { onNavigate("purchase") } }
                item { MenuCard("销售单", "销售订单查询", Icons.Default.PointOfSale, Green500) { onNavigate("sales") } }
                item { MenuCard("客户管理", "客户信息浏览", Icons.Default.People, Teal500) { onNavigate("customer") } }
                item { MenuCard("供应商管理", "供应商信息浏览", Icons.Default.Business, Grey600) { onNavigate("supplier") } }
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
