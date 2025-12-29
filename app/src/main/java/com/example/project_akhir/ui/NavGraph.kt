package com.example.project_akhir.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.project_akhir.ui.screens.AuthScreen
import com.example.project_akhir.ui.screens.HomeScreen
import com.example.project_akhir.ui.screens.AddProductScreen
import com.example.project_akhir.ui.screens.ProductDetailScreen
import com.example.project_akhir.ui.screens.ProfileScreen

@Composable
fun NavGraph(startDestination: String = "auth") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        // 1. Layar Login & Daftar
        composable("auth") {
            AuthScreen(onLoginSuccess = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        // 2. Layar Katalog Utama
        composable("home") {
            HomeScreen(
                onAddProductClick = {
                    navController.navigate("add_product")
                },
                onProductClick = { productId ->
                    // Navigasi ke detail dengan membawa ID produk
                    navController.navigate("detail/$productId")
                },
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        }

        // 3. Layar Tambah Barang (Format Lengkap sesuai RAT C.2)
        composable("add_product") {
            AddProductScreen(onSuccess = {
                navController.popBackStack()
            })
        }

        // 4. Layar Detail Produk (Menampilkan Deskripsi & Tombol WA)
        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(productId = productId)
        }

        // 5. Layar Profil Pengguna (Sesuai RAT C.1)
        composable("profile") {
            ProfileScreen(onLogout = {
                navController.navigate("auth") {
                    popUpTo(0) // Bersihkan semua history agar tidak bisa back setelah logout
                }
            })
        }
    }
}