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
    paddingValues: PaddingValues,
    startDestination: String = "splash"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        // Modifier padding memastikan konten tidak tertutup Bottom Bar
        modifier = Modifier.padding(paddingValues)
    ) {

        // 1. Splash Screen (Animasi Logo)
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        // 2. Onboarding Screen (Slider Sambutan)
        composable("onboarding") {
            OnboardingScreen(onFinish = {
                navController.navigate("auth") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }

        // 3. Layar Login & Daftar (Auth)
        composable("auth") {
            AuthScreen(onLoginSuccess = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        // 4. Layar Katalog Utama (Grid, Search, & Filter)
        composable("home") {
            HomeScreen(
                onAddProductClick = { navController.navigate("add_product") },
                onProductClick = { id -> navController.navigate("detail/$id") },
                onProfileClick = { navController.navigate("profile") }
            )
        }

        // 5. Layar Tambah Barang (Logika Base64)
        composable("add_product") {
            AddProductScreen(onSuccess = {
                navController.popBackStack()
            })
        }

        // 6. Layar Detail Produk (WhatsApp & Deskripsi)
        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(productId = productId)
        }

        // 7. Layar Profil Pengguna
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) // Clear history setelah logout
                    }
                },
                // Navigasi ke halaman edit profil yang baru
                onEditProfileClick = {
                    navController.navigate("edit_profile")
                }
            )
        }

        // 8. Layar Edit Profil (Fungsi Baru)
        composable("edit_profile") {
            EditProfileScreen(onBack = {
                navController.popBackStack()
            })
        }

        // 9. Layar Iklan Saya (Manajemen & Hapus)
        composable("my_ads") {
            MyAdsScreen(onProductClick = { id ->
                navController.navigate("detail/$id")
            })
        }
    }
}