package com.example.inventory.ui.customer

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.ui.component.SearchBar
import com.example.inventory.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerManageScreen(
    onBack: () -> Unit,
    viewModel: CustomerManageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteConfirmId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("客户管理", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchBar(
                query = searchText,
                onQueryChange = { searchText = it; viewModel.search(it) },
                placeholder = "搜索客户"
            )
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.customers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无客户", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.customers, key = { it.id }) { customer ->
                        CustomerRow(
                            customer = customer,
                            onDelete = { deleteConfirmId = customer.id }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { code, name, contact, phone, addr, note ->
                viewModel.insert(code, name, contact, phone, addr, note)
                showAddDialog = false
            }
        )
    }

    deleteConfirmId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除此客户吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(id)
                    deleteConfirmId = null
                }) { Text("删除", color = Red500) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) { Text("取消") }
            }
        )
    }
}

@Composable
fun CustomerRow(customer: com.example.inventory.data.local.model.Customer, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, null, tint = Teal500, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(customer.code, fontSize = 12.sp, color = Grey600)
                if (customer.phone.isNotBlank()) Text("☎ ${customer.phone}", fontSize = 12.sp, color = Grey600)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "删除", tint = Red500, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String, name: String, contact: String, phone: String, address: String, note: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增客户") },
        text = {
            Column {
                OutlinedTextField(code, { code = it }, label = { Text("编码 *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(name, { name = it }, label = { Text("名称 *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(contact, { contact = it }, label = { Text("联系人") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(phone, { phone = it }, label = { Text("电话") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(address, { address = it }, label = { Text("地址") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(note, { note = it }, label = { Text("备注") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (code.isNotBlank() && name.isNotBlank()) onConfirm(code, name, contact, phone, address, note) },
                enabled = code.isNotBlank() && name.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
