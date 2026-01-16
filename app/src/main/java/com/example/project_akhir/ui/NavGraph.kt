package com.example.project_akhir.ui

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
import com.example.project_akhir.ui.screens.AddProductScreen
import com.example.project_akhir.view.screens.EditProfileScreen
import com.example.project_akhir.view.screens.HomeScreen
import com.example.project_akhir.ui.screens.MyAdsScreen
import com.example.project_akhir.ui.screens.ProductDetailScreen
import com.example.project_akhir.view.screens.ProfileScreen
import com.example.project_akhir.view.screens.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    startDestination: String = "splash"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {

        // 1. Splash Screen
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        // 2. Onboarding Screen
        composable("onboarding") {
            OnboardingScreen(onFinish = {
                navController.navigate("auth") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }

        // 3. Layar Auth
        composable("auth") {
            AuthScreen(onLoginSuccess = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        // 4. Layar Home
        composable("home") {
            HomeScreen(
                onAddProductClick = { navController.navigate("add_product") },
                onProductClick = { id -> navController.navigate("detail/$id") },
                onProfileClick = { navController.navigate("profile") }
            )
        }

        // 5. Layar Tambah Barang
        composable("add_product") {
            AddProductScreen(onSuccess = {
                navController.popBackStack()
            })
        }

        // 6. Layar Detail Produk (DIPERBAIKI)
        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                navController = navController, // Ditambahkan untuk logika login WhatsApp
                onNavigateToEdit = { id -> navController.navigate("edit_product/$id") },
                onBack = { navController.popBackStack() }
            )
        }

        // 7. Layar Edit Produk (WAJIB DITAMBAHKAN AGAR TIDAK ERROR)
        composable(
            route = "edit_product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            EditProductScreen(
                productId = productId,
                onSuccess = { navController.popBackStack() }
            )
        }

        // 8. Layar Profil Pengguna
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("auth") { popUpTo(0) }
                },
                onEditProfileClick = { navController.navigate("edit_profile") }
            )
        }

        // 9. Layar Edit Profil
        composable("edit_profile") {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }

        // 10. Layar Iklan Saya (DIPERBAIKI)
        composable("my_ads") {
            MyAdsScreen(
                onProductClick = { id -> navController.navigate("detail/$id") },
                onEditClick = { id -> navController.navigate("edit_product/$id") }
            )
        }
    }
}