package com.example.inventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.inventory.ui.customer.CustomerListScreen
import com.example.inventory.ui.home.HomeScreen
import com.example.inventory.ui.inventory.InventoryScreen
import com.example.inventory.ui.product.ProductDetailScreen
import com.example.inventory.ui.purchase.PurchaseListScreen
import com.example.inventory.ui.sales.SalesListScreen
import com.example.inventory.ui.supplier.SupplierListScreen

object Routes {
    const val HOME = "home"
    const val INVENTORY = "inventory"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val PURCHASE = "purchase"
    const val SALES = "sales"
    const val CUSTOMER = "customer"
    const val SUPPLIER = "supplier"

    fun productDetail(id: Long) = "product/$id"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Routes.INVENTORY) {
            InventoryScreen(
                onBack = { navController.popBackStack() },
                onProductClick = { id -> navController.navigate(Routes.productDetail(id)) }
            )
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            ProductDetailScreen(
                productId = productId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PURCHASE) {
            PurchaseListScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SALES) {
            SalesListScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CUSTOMER) {
            CustomerListScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SUPPLIER) {
            SupplierListScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
