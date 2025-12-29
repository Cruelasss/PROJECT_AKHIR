package com.example.project_akhir.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project_akhir.ui.navigation.NavGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // State untuk memantau rute aktif secara otomatis
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Container navigasi bawah
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                actions = {
                    // Tombol HOME
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )

                    // Tombol PESAN (Placeholder)
                    NavigationBarItem(
                        selected = currentRoute == "messages",
                        onClick = { /* Implementasi nanti jika ada */ },
                        icon = { Icon(Icons.Default.Email, contentDescription = "Pesan") },
                        label = { Text("Pesan") }
                    )

                    // Memberikan ruang untuk tombol JUAL di tengah
                    Spacer(modifier = Modifier.weight(1f))

                    // Tombol IKLAN SAYA
                    NavigationBarItem(
                        selected = currentRoute == "my_ads",
                        onClick = {
                            navController.navigate("my_ads") {
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.List, contentDescription = "Iklan Saya") },
                        label = { Text("Iklan Saya") }
                    )

                    // Tombol AKUN
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Akun") },
                        label = { Text("Akun") }
                    )
                }
            )
        },
        floatingActionButton = {
            // Tombol JUAL yang menonjol di tengah
            LargeFloatingActionButton(
                onClick = { navController.navigate("add_product") },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier
                    .size(65.dp)
                    .offset(y = 50.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Jual",
                    tint = Color.White,
                    modifier = Modifier.size(35.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        // Menghubungkan NavController ke NavGraph agar navigasi sinkron
        NavGraph(
            navController = navController,
            paddingValues = innerPadding
        )
    }
}