package com.example.project_akhir.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.project_akhir.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddProductClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var productList by remember { mutableStateOf(listOf<Product>()) }

    // State untuk Pencarian dan Filter Lokasi
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("Semua") }

    // State untuk Banner Promo
    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(Unit) {
        db.collection("products").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) productList = snapshot.toObjects<Product>()
        }
    }

    val locations = remember(productList) {
        listOf("Semua") + productList.map { it.city_location }.distinct().sorted()
    }

    val filteredProducts = productList.filter { product ->
        val matchesSearch = product.title.contains(searchQuery, ignoreCase = true)
        val matchesLocation = if (selectedLocation == "Semua") true
        else product.city_location.equals(selectedLocation, ignoreCase = true)
        matchesSearch && matchesLocation
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary).padding(bottom = 8.dp)) {
                // Header Lokasi & Notifikasi
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                    Text(
                        text = selectedLocation,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Notifications, tint = Color.White, contentDescription = null)
                    }
                }

                // Search Bar Modern
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Temukan Mobil, Handphone...") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Banner Promo (Carousel)
            item(span = { GridItemSpan(2) }) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            "DISKON AKHIR TAHUN BEKASINAJA",
                            modifier = Modifier.align(Alignment.Center),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // 2. Filter Lokasi (Horizontal Chips)
            item(span = { GridItemSpan(2) }) {
                ScrollableTabRow(
                    selectedTabIndex = locations.indexOf(selectedLocation),
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {}
                ) {
                    locations.forEach { location ->
                        FilterChip(
                            selected = selectedLocation == location,
                            onClick = { selectedLocation = location },
                            label = { Text(location) },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // 3. Kategori Ikon
            item(span = { GridItemSpan(2) }) {
                val categories = listOf("Mobil", "Properti", "Motor", "Jasa", "Elektronik")
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(categories) { cat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 20.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE3F2FD),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Icon(
                                    Icons.Default.List,
                                    contentDescription = null,
                                    modifier = Modifier.padding(12.dp),
                                    tint = Color(0xFF1976D2)
                                )
                            }
                            Text(cat, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }

            // 4. Grid Produk
            items(filteredProducts) { product ->
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
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Logika Gambar (Base64)
            val bitmap = remember(product.images) {
                try {
                    val imageString = product.images.firstOrNull()
                    if (!imageString.isNullOrEmpty()) {
                        val imageBytes = android.util.Base64.decode(imageString, android.util.Base64.DEFAULT)
                        android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    } else null
                } catch (e: Exception) { null }
            }

            Box {
                AsyncImage(
                    model = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop
                )
                // Badge Kondisi
                Surface(
                    color = Color(0xFF002F34).copy(alpha = 0.8f),
                    modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(4.dp))
                ) {
                    Text(
                        product.condition,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // PERBAIKAN: Format harga agar tidak muncul 1.2E8
                val formattedPrice = try {
                    product.price.toDouble().let {
                        java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID")).format(it)
                    }
                } catch (e: Exception) { "Rp ${product.price}" }

                Text(
                    text = formattedPrice,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(text = product.title, maxLines = 1, fontSize = 14.sp)
                Text(text = "📍 ${product.city_location}", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}