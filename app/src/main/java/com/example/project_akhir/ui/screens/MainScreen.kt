package com.example.project_akhir.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project_akhir.ui.NavGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBarRoutes = listOf("splash", "onboarding", "auth")
    val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                // Gunakan Surface untuk memberikan bayangan halus pada bar bawah
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp,
                    color = Color.White
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        modifier = Modifier.height(80.dp) // Bar dibuat lebih tinggi agar teks aman
                    ) {
                        // 1. HOME
                        NavigationBarItem(
                            selected = currentRoute == "home",
                            onClick = { navController.navigate("home") },
                            icon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(26.dp)) },
                            label = { Text("Home", fontWeight = FontWeight.Medium) }
                        )

                        // 2. SPACER ADAPTIF (Memberikan celah agar tombol Jual tidak menabrak label)
                        // Kita gunakan dua spacer kecil di kiri-kanan tombol tengah
                        Spacer(modifier = Modifier.weight(0.5f))

                        // Ruang kosong utama untuk FAB
                        Box(modifier = Modifier.weight(1f))

                        Spacer(modifier = Modifier.weight(0.5f))

                        // 3. IKLAN
                        NavigationBarItem(
                            selected = currentRoute == "my_ads",
                            onClick = { navController.navigate("my_ads") },
                            icon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(26.dp)) },
                            label = { Text("Iklan", fontWeight = FontWeight.Medium) }
                        )

                        // 4. AKUN
                        NavigationBarItem(
                            selected = currentRoute == "profile",
                            onClick = { navController.navigate("profile") },
                            icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(26.dp)) },
                            label = { Text("Akun", fontWeight = FontWeight.Medium) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (shouldShowBottomBar) {
                // Tombol JUAL yang proporsional
                FloatingActionButton(
                    onClick = { navController.navigate("add_product") },
                    containerColor = Color(0xFF002F34),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(68.dp) // Ukuran optimal agar tidak "gak rapi"
                        .offset(y = 45.dp) // Menurunkan posisi agar melayang pas di tengah bar
                        .border(4.dp, Color.White, CircleShape) // Ring putih agar kontras dan rapi
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Jual",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavGraph(navController = navController, paddingValues = innerPadding)
    }
}