package com.example.inventory.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.data.local.model.Product
import com.example.inventory.data.repository.ProductRepository
import com.example.inventory.ui.component.SearchBar
import com.example.inventory.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchBar(
                query = searchText,
                onQueryChange = { searchText = it; viewModel.search(it) },
                placeholder = "模糊搜索：商品编码/名称/拼音码/条码"
            )

            if (uiState.products.isNotEmpty()) {
                Text("共 ${uiState.products.size} 条", fontSize = 12.sp, color = Grey600,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("未找到匹配商品", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductCard(product)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "新增商品")
        }
    }

    if (showAddDialog) {
        ProductEntryDialog(
            onDismiss = { showAddDialog = false },
            onSaved = { viewModel.loadProducts() }
        )
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(product.code, fontSize = 12.sp, color = Grey600)
            }
            if (product.spec.isNotBlank()) Text("规格: ${product.spec}", fontSize = 12.sp, color = Grey600, modifier = Modifier.padding(top = 2.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PriceTag("成本价", product.costPrice, Orange500)
                PriceTag("批发价", product.wholesalePrice, Blue700)
                PriceTag("零售价", product.retailPrice, Green500)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (product.barcode.isNotBlank()) Text("条码: ${product.barcode}", fontSize = 11.sp, color = Grey600)
                Text("单位: ${product.unit}", fontSize = 11.sp, color = Grey600)
            }
        }
    }
}

@Composable
fun PriceTag(label: String, price: Double, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("¥%.2f".format(price), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = Grey600)
    }
}

@Composable
fun ProductEntryDialog(onDismiss: () -> Unit, onSaved: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("个") }
    var spec by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var wholesalePrice by remember { mutableStateOf("") }
    var retailPrice by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增商品") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(code, { code = it }, label = { Text("编码 *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(name, { name = it }, label = { Text("名称 *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(barcode, { barcode = it }, label = { Text("条码") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(unit, { unit = it }, label = { Text("单位") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(spec, { spec = it }, label = { Text("规格") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(costPrice, { costPrice = it }, label = { Text("成本价") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(wholesalePrice, { wholesalePrice = it }, label = { Text("批发价") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(retailPrice, { retailPrice = it }, label = { Text("零售价") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(enabled = code.isNotBlank() && name.isNotBlank() && !isSaving, onClick = {
                isSaving = true
                scope.launch(Dispatchers.IO) {
                    val repo = ProductRepository(context)
                    repo.insert(code = code, name = name, barcode = barcode, unit = unit, spec = spec,
                        costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                        wholesalePrice = wholesalePrice.toDoubleOrNull() ?: 0.0,
                        retailPrice = retailPrice.toDoubleOrNull() ?: 0.0)
                    scope.launch(Dispatchers.Main) { onSaved(); onDismiss() }
                }
            }) { Text(if (isSaving) "保存中..." else "保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
