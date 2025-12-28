package com.example.project_akhir.ui.screens

import android.R.id.primary
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project_akhir.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAddProductClick: () -> Unit) {
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
            TopAppBar(title = { Text("BekasinAja - Katalog") })
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
                ProductItem(product)
            }
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = product.title, style = MaterialTheme.typography.titleLarge) //

            Text(
                text = "Rp ${product.price}",
                color = MaterialTheme.colorScheme.primary // Gunakan colorScheme.primary
            )
            Text(text = "Lokasi: ${product.city_location}") //
            Text(text = "Kondisi: ${product.condition}", style = MaterialTheme.typography.bodySmall) //
        }
    }
}