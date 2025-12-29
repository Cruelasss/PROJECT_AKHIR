package com.example.project_akhir.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.project_akhir.ui.screens.*

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues, // Penting agar konten tidak tertutup Bottom Bar
    startDestination: String = "auth"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues) // Menerapkan padding dari Scaffold utama
    ) {

        // 1. Layar Login & Daftar
        composable("auth") {
            AuthScreen(onLoginSuccess = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        // 2. Layar Katalog Utama (Grid, Search, & Banner)
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

        // 3. Layar Tambah Barang (Menggunakan Logika Base64 - Tanpa Storage)
        composable("add_product") {
            AddProductScreen(onSuccess = {
                navController.popBackStack()
            })
        }

        // 4. Layar Detail Produk (Menampilkan Gambar Base64 & Tombol WA)
        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(productId = productId) //
        }

        // 5. Layar Profil Pengguna (Sesuai RAT C.1)
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0)
                    }
                }
            )
        }

        // 6. Layar Iklan Saya (Opsional - Jika Anda ingin menambahkannya nanti)
        composable("my_ads") {
            MyAdsScreen(onProductClick = { productId ->
                navController.navigate("detail/$productId")
            })
        }
    }
}