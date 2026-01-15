package com.example.project_akhir.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.project_akhir.model.Product
import com.example.project_akhir.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ProductDetailScreen(productId: String, onNavigateToEdit: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var product by remember { mutableStateOf<Product?>(null) }
    var seller by remember { mutableStateOf<User?>(null) }

    // State untuk fitur baru
    var isFullScreen by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }

    // Logic Fetch Data (Tetap sama dengan milik Anda)
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

    // --- FITUR 1: FOTO FULL SCREEN MODAL ---
    if (isFullScreen && product != null) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                val pagerState = rememberPagerState(pageCount = { product!!.images.size })
                HorizontalPager(state = pagerState) { index ->
                    val bitmap = remember(product!!.images[index]) {
                        try {
                            val imageBytes = Base64.decode(product!!.images[index], Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        } catch (e: Exception) { null }
                    }
                    AsyncImage(
                        model = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                IconButton(
                    onClick = { isFullScreen = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(32.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Barang") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                }
            )
        },
        bottomBar = {
            product?.let { item ->
                val isOwner = currentUser?.uid == item.sellerId
                Surface(shadowElevation = 8.dp) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        if (isOwner) {
                            // TOMBOL PEMILIK (Bisa Edit & Hapus)
                            Button(onClick = { onNavigateToEdit(item.productId) }, modifier = Modifier.weight(1f)) {
                                Text("Edit")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    db.collection("products").document(productId).delete().addOnSuccessListener { onBack() }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Hapus", color = Color.Red)
                            }
                        } else {
                            // TOMBOL PENGUNJUNG (WA + Proteksi Login)
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                onClick = {
                                    if (currentUser == null) {
                                        // Trigger ke halaman login jika belum login
                                        // navController.navigate("login")
                                    } else {
                                        seller?.phone_number?.let { phone ->
                                            val url = "https://wa.me/$phone?text=Halo, saya tertarik dengan ${item.title}"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (currentUser == null) "Login untuk WhatsApp" else "Hubungi Penjual")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        product?.let { item ->
            Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {

                // --- SLIDER FOTO (Klik untuk Full Screen) ---
                val pagerState = rememberPagerState(pageCount = { item.images.size })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(300.dp).clickable { isFullScreen = true }
                ) { index ->
                    val bitmap = remember(item.images[index]) {
                        try {
                            val imageBytes = Base64.decode(item.images[index], Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        } catch (e: Exception) { null }
                    }
                    AsyncImage(
                        model = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Judul & Harga (Formatted agar tidak muncul 1.2E8)
                    val formattedPrice = try {
                        NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(item.price)
                    } catch (e: Exception) { "Rp ${item.price}" }

                    Text(item.title, style = MaterialTheme.typography.headlineMedium)
                    Text(formattedPrice, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

                    // --- FITUR: KATEGORI & TANGGAL ---
                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        SuggestionChip(onClick = {}, label = { Text(item.category) })
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(onClick = {}, label = { Text(item.condition) })
                    }

                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                        Date(
                            item.createdAt
                        )
                    )
                    Text("Diposting pada: $dateStr", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // Info Penjual & Lokasi
                    Text("Penjual: ${seller?.full_name ?: "Memuat..."}", fontWeight = FontWeight.Bold)
                    Text("📍 Lokasi: ${item.city_location}")

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Deskripsi:", style = MaterialTheme.typography.titleSmall)
                    Text(item.description)
                }
            }
        } ?: CenterCircularProgress()
    }
}

@Composable
fun CenterCircularProgress() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}