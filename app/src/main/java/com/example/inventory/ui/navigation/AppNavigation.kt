package com.example.inventory.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.inventory.ui.catalog.CatalogScreen
import com.example.inventory.ui.home.HomeScreen
import com.example.inventory.ui.inventory.InventoryScreen
import com.example.inventory.ui.product.ProductDetailScreen
import com.example.inventory.ui.purchase.PurchaseListScreen
import com.example.inventory.ui.sales.SalesListScreen
import com.example.inventory.ui.theme.Blue700
import com.example.inventory.ui.theme.Grey600

enum class MainTab(val route: String, val label: String) {
    DASHBOARD("dashboard", "首页"),
    CATALOG("catalog", "资料"),
    INVENTORY("inventory", "库存"),
    PURCHASE("purchase", "进货"),
    SALES("sales", "销售")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in MainTab.entries.map { it.route }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("进销存查询", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    MainTab.entries.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(tab.route) {
                                        popUpTo(MainTab.DASHBOARD.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    when (tab) {
                                        MainTab.DASHBOARD -> Icons.Default.Dashboard
                                        MainTab.CATALOG -> Icons.Default.Search
                                        MainTab.INVENTORY -> Icons.Default.Inventory
                                        MainTab.PURCHASE -> Icons.Default.ShoppingCart
                                        MainTab.SALES -> Icons.Default.TrendingUp
                                    },
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Blue700,
                                selectedTextColor = Blue700,
                                unselectedIconColor = Grey600,
                                unselectedTextColor = Grey600
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.DASHBOARD.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTab.DASHBOARD.route) {
                HomeScreen(onNavigate = { id -> navController.navigate("product/$id") })
            }
            composable(MainTab.CATALOG.route) {
                CatalogScreen()
            }
            composable(MainTab.INVENTORY.route) {
                InventoryScreen(onProductClick = { id -> navController.navigate("product/$id") })
            }
            composable(MainTab.PURCHASE.route) {
                PurchaseListScreen()
            }
            composable(MainTab.SALES.route) {
                SalesListScreen()
            }
            composable(
                route = "product/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { entry ->
                val productId = entry.arguments?.getLong("productId") ?: 0L
                ProductDetailScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
