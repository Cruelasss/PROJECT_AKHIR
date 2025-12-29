package com.example.project_akhir.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.project_akhir.model.Product
import com.example.project_akhir.model.User
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var product by remember { mutableStateOf<Product?>(null) }
    var seller by remember { mutableStateOf<User?>(null) }

    // Ambil Data Produk & Penjual secara Relasional
    LaunchedEffect(productId) {
        db.collection("products").document(productId).get().addOnSuccessListener { pSnap ->
            product = pSnap.toObject(Product::class.java)
            product?.sellerId?.let { sId ->
                db.collection("users").document(sId).get().addOnSuccessListener { uSnap ->
                    seller = uSnap.toObject(User::class.java)
                }
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Detail Barang") }) }) { padding ->
        product?.let { item ->
            Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
                // Tampilkan Gambar dari Firebase Storage
                AsyncImage(
                    model = item.images.firstOrNull(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(item.title, style = MaterialTheme.typography.headlineMedium) //
                    Text("Rp ${item.price}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) //

                    Spacer(modifier = Modifier.height(8.dp))
                    Badge { Text(item.condition) } // Baru/Bekas/Rusak

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Text("Lokasi: ${item.city_location}") //
                    Text("Deskripsi:", style = MaterialTheme.typography.titleSmall)
                    Text(item.description) //

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tombol Hubungi Penjual (WhatsApp Intent)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            seller?.phone_number?.let { phone ->
                                val url = "https://wa.me/$phone?text=Halo, saya tertarik dengan ${item.title}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        }
                    ) {
                        Text("Hubungi Penjual (WhatsApp)")
                    }
                }
            }
        } ?: CenterCircularProgress()
    }
}

@Composable
fun CenterCircularProgress() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        CircularProgressIndicator()
    }
}