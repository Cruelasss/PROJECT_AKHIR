package com.example.project_akhir.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project_akhir.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddProductClick: () -> Unit,
    onProductClick: (String) -> Unit, // Parameter baru untuk ke Detail
    onProfileClick: () -> Unit        // Parameter baru untuk ke Profil
) {
    val db = FirebaseFirestore.getInstance()
    var productList by remember { mutableStateOf(listOf<Product>()) }

    // Mengambil data real-time dari Firestore
    LaunchedEffect(Unit) {
        db.collection("products").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                productList = snapshot.toObjects<Product>()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BekasinAja - Katalog") },
                actions = {
                    // Tombol untuk membuka profil (Sesuai RAT C.1)
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profil")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProductClick) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(productList) { product ->
                // Membungkus item dengan clickable agar bisa navigasi ke detail
                ProductItem(
                    product = product,
                    onClick = { onProductClick(product.productId) }
                )
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Menangani klik pada kartu barang
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Menampilkan Title
            Text(text = product.title, style = MaterialTheme.typography.titleLarge)

            // Menampilkan Harga dengan warna tema utama
            Text(
                text = "Rp ${product.price}",
                color = MaterialTheme.colorScheme.primary
            )

            // Menampilkan Lokasi & Kondisi sesuai format RAT
            Text(text = "📍 ${product.city_location}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Kondisi: ${product.condition}", style = MaterialTheme.typography.bodySmall)
        }
    }
}