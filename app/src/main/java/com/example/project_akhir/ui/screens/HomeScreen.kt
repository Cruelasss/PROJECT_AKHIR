package com.example.project_akhir.view.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.project_akhir.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddProductClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var productList by remember { mutableStateOf(listOf<Product>()) }

    // 1. State Filter & Pencarian
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("Semua Indonesia") } // Default semua provinsi
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showPriceFilter by remember { mutableStateOf(false) } // Untuk filter < 500rb

    // State untuk Banner
    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(Unit) {
        db.collection("products").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) productList = snapshot.toObjects<Product>()
        }
    }

    // 2. Logika Filter Asinkron (Tanpa Reload)
    val filteredProducts = remember(productList, searchQuery, selectedLocation, selectedCategory, showPriceFilter) {
        productList.filter { product ->
            val matchesSearch = product.title.contains(searchQuery, ignoreCase = true)
            val matchesLocation = if (selectedLocation == "Semua Indonesia") true
            else product.city_location == selectedLocation
            val matchesCategory = if (selectedCategory == "Semua") true
            else product.category == selectedCategory
            val matchesPrice = if (showPriceFilter) product.price < 500000 else true

            matchesSearch && matchesLocation && matchesCategory && matchesPrice
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary).padding(bottom = 8.dp)) {
                // Header: Ganti Icon Notifikasi dengan Logo (Nanti ganti R.drawable.logo_anda)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dropdown Lokasi (Provinsi)
                    LocationDropdown(
                        selectedLocation = selectedLocation,
                        onLocationSelected = { selectedLocation = it }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Ganti lambang notifikasi ke Logo BekasinAja
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Ganti ke logo anda
                            tint = Color.White,
                            contentDescription = "Logo BekasinAja",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Search Bar & Filter Button
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari barang bekas...") },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        colors = TextFieldDefaults.colors( Color.White),
                        singleLine = true
                    )

                    // Tombol Filter Harga < 500rb
                    IconButton(
                        onClick = { showPriceFilter = !showPriceFilter },
                        modifier = Modifier.padding(start = 8.dp).background(if(showPriceFilter) Color.Yellow else Color.White, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.Black)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Banner Carousel
            item(span = { GridItemSpan(2) }) {
                HorizontalPager(state = pagerState, modifier = Modifier.height(140.dp).padding(16.dp)) {
                    PromoBanner()
                }
            }

            // 3. Kategori Ikon (Bisa di Klik)
            item(span = { GridItemSpan(2) }) {
                CategoryList(
                    selectedCategory = selectedCategory,
                    onCategoryClick = { selectedCategory = it }
                )
            }

            // Status Filter Aktif
            if (showPriceFilter) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        "Menampilkan Harga < Rp 500.000",
                        modifier = Modifier.padding(16.dp, 0.dp),
                        fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold
                    )
                }
            }

            // Grid Produk
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
fun LocationDropdown(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val provinces = listOf(
        "Semua Indonesia", "Aceh", "Bali", "Banten", "Bengkulu", "DIY Yogyakarta",
        "DKI Jakarta", "Gorontalo", "Jambi", "Jawa Barat", "Jawa Tengah", "Jawa Timur",
        "Kalimantan Barat", "Kalimantan Selatan", "Kalimantan Tengah", "Kalimantan Timur",
        "Kalimantan Utara", "Kepulauan Bangka Belitung", "Kepulauan Riau", "Lampung",
        "Maluku", "Maluku Utara", "Nusa Tenggara Barat", "Nusa Tenggara Timur", "Papua",
        "Riau", "Sulawesi Barat", "Sulawesi Selatan", "Sulawesi Tengah", "Sulawesi Tenggara",
        "Sulawesi Utara", "Sumatera Barat", "Sumatera Selatan", "Sumatera Utara"
    )

    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
            Text(
                text = selectedLocation,
                color = Color.White,
                modifier = Modifier.padding(start = 4.dp),
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            provinces.forEach { province ->
                DropdownMenuItem(
                    text = { Text(province) },
                    onClick = {
                        onLocationSelected(province)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PromoBanner() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF002F34))
    ) {
        Text(
            "DISKON AKHIR TAHUN BEKASINAJA",
            modifier = Modifier.align(Alignment.Center),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CategoryList(
    selectedCategory: String,
    onCategoryClick: (String) -> Unit
) {
    val categories = listOf("Semua", "Mobil", "Properti", "Motor", "Jasa", "Elektronik", "Hobi")
    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { cat ->
            FilterChip(
                selected = selectedCategory == cat,
                onClick = { onCategoryClick(cat) },
                label = { Text(cat) },
                modifier = Modifier.padding(end = 8.dp)
            )
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
            // --- LOGIKA GAMBAR (Format Sebelumnya) ---
            val bitmap = remember(product.images) {
                try {
                    val imageString = product.images.firstOrNull()
                    if (!imageString.isNullOrEmpty()) {
                        val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    } else null
                } catch (e: Exception) { null }
            }

            Box {
                if (bitmap != null) {
                    AsyncImage(
                        model = bitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder jika gambar tidak ada
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color.LightGray)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center),
                            tint = Color.Gray
                        )
                    }
                }

                // Badge Kondisi (Baru/Bekas)
                Surface(
                    color = Color(0xFF002F34).copy(alpha = 0.8f),
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = product.condition,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // Format harga agar rapi (Rp 100.000)
                val formattedPrice = try {
                    NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(product.price)
                } catch (e: Exception) { "Rp ${product.price}" }

                Text(
                    text = formattedPrice,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1976D2) // Memberi warna pada harga agar lebih menarik
                )

                Text(
                    text = product.title,
                    maxLines = 1,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "📍 ${product.city_location}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tanggal Posting
                val dateStr = try {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(product.createdAt))
                } catch (e: Exception) { "" }

                Text(
                    text = "Diposting: $dateStr",
                    fontSize = 9.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}