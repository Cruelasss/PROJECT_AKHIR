package com.example.project_akhir.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project_akhir.ui.screens.AuthScreen
import com.example.project_akhir.ui.screens.HomeScreen
import com.example.project_akhir.ui.screens.AddProductScreen

@Composable
fun NavGraph(startDestination: String = "auth") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        // Layar Login & Daftar
        composable("auth") {
            AuthScreen(onLoginSuccess = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true } // Hapus screen login dari history
                }
            })
        }

        // Layar Katalog Utama
        composable("home") {
            HomeScreen(onAddProductClick = {
                navController.navigate("add_product")
            })
        }

        // Layar Tambah Barang (Sesuai RAT C.2)
        composable("add_product") {
            AddProductScreen(onSuccess = {
                navController.popBackStack() // Kembali ke Home setelah berhasil simpan
            })
        }
    }
}