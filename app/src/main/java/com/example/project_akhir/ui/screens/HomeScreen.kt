package com.example.project_akhir.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.project_akhir.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddProductClick: () -> Unit,
    onProductClick: (String) -> Unit, // Navigasi ke Detail
    onProfileClick: () -> Unit        // Navigasi ke Profil
) {
    val db = FirebaseFirestore.getInstance()
    var productList by remember { mutableStateOf(listOf<Product>()) }

    // State untuk Fitur Pencarian dan Filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("Semua Lokasi") }

    // Ambil data real-time dari Firestore
    LaunchedEffect(Unit) {
        db.collection("products").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                productList = snapshot.toObjects<Product>()
            }
        }
    }

    // Logika Filter: Menyaring produk berdasarkan judul dan lokasi
    val filteredProducts = productList.filter { product ->
        val matchesSearch = product.title.contains(searchQuery, ignoreCase = true)
        val matchesLocation = if (selectedLocation == "Semua Lokasi") true
        else product.city_location.equals(selectedLocation, ignoreCase = true)
        matchesSearch && matchesLocation
    }

    // Mendapatkan daftar lokasi unik dari database untuk tombol filter
    val locations = remember(productList) {
        listOf("Semua Lokasi") + productList.map { it.city_location }.distinct().sorted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BekasinAja") },
                actions = {
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
        // Menggunakan Grid 2 Kolom agar tampilan lebih menarik
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(productList) { product ->
                ProductGridItem(
                    product = product,
                    onClick = { onProductClick(product.productId) }
                )
            }
        }
    }
}

@Composable
fun ProductGridItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // LOGIKA DECODE BASE64: Menampilkan foto yang disimpan di Firestore
            val bitmap = remember(product.images) {
                try {
                    val imageString = product.images.firstOrNull()
                    if (!imageString.isNullOrEmpty()) {
                        val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }

            AsyncImage(
                model = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = product.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    text = "Rp ${product.price}",
                    color = MaterialTheme.colorScheme.primary, //
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(text = product.city_location, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}