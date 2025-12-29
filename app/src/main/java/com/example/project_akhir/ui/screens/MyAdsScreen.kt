package com.example.project_akhir.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.project_akhir.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAdsScreen(onProductClick: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var myProducts by remember { mutableStateOf(listOf<Product>()) }
    val currentUserId = auth.currentUser?.uid ?: ""

    // State untuk Dialog Konfirmasi
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedProductId by remember { mutableStateOf("") }

    // Ambil data milik user yang sedang login secara real-time
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            db.collection("products")
                .whereEqualTo("sellerId", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        myProducts = snapshot.toObjects<Product>()
                    }
                }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Iklan Saya") }) }
    ) { padding ->
        if (myProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Anda belum memiliki iklan aktif.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(myProducts) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        onClick = { onProductClick(product.productId) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Rp ${product.price}", color = MaterialTheme.colorScheme.primary)
                                Text("Kondisi: ${product.condition}", style = MaterialTheme.typography.bodySmall)
                            }

                            // Tombol Picu Hapus
                            IconButton(onClick = {
                                selectedProductId = product.productId // Simpan ID yang mau dihapus
                                showDeleteDialog = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        // Dialog Konfirmasi Hapus
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hapus Iklan?") },
                text = { Text("Apakah Anda yakin ingin menghapus iklan ini? Tindakan ini tidak dapat dibatalkan.") },
                confirmButton = {
                    TextButton(onClick = {
                        if (selectedProductId.isNotEmpty()) {
                            db.collection("products").document(selectedProductId).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Iklan berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    showDeleteDialog = false
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }) {
                        Text("Hapus", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}