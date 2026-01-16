package com.example.project_akhir.ui.screens

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
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
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyAdsScreen(
    onProductClick: (String) -> Unit,
    onEditClick: (String) -> Unit // Tambahkan parameter navigasi edit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var myProducts by remember { mutableStateOf(listOf<Product>()) }
    val currentUserId = auth.currentUser?.uid ?: ""

    // State untuk Dialog Konfirmasi
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedProductId by remember { mutableStateOf("") }

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
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Anda belum memiliki iklan aktif.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(myProducts) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        onClick = { onProductClick(product.productId) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. TAMPILAN GAMBAR (Base64)
                            val bitmap = remember(product.images) {
                                try {
                                    val imgString = product.images.firstOrNull()
                                    if (!imgString.isNullOrEmpty()) {
                                        val bytes = Base64.decode(imgString, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } else null
                                } catch (e: Exception) { null }
                            }

                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray)) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.align(Alignment.Center))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // 2. INFO PRODUK
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.title, fontWeight = FontWeight.Bold, maxLines = 1)

                                // Format Harga Rupiah
                                val formattedPrice = try {
                                    NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(product.price)
                                } catch (e: Exception) { "Rp ${product.price}" }

                                Text(formattedPrice, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

                                // Info Kategori & Lokasi
                                Text("${product.category} • ${product.city_location}", fontSize = 11.sp, color = Color.Gray)
                            }

                            // 3. TOMBOL AKSI (EDIT & HAPUS)
                            Row {
                                IconButton(onClick = { onEditClick(product.productId) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF1976D2))
                                }
                                IconButton(onClick = {
                                    selectedProductId = product.productId
                                    showDeleteDialog = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog Konfirmasi Hapus Tetap Sama
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
                        }
                    }) { Text("Hapus", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
                }
            )
        }
    }
}