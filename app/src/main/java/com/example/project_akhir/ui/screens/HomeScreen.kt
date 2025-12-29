package com.example.project_akhir.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan // Perbaikan Import
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
    var searchQuery by remember { mutableStateOf("") }

    // State untuk Banner Carousel
    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(Unit) {
        db.collection("products").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) productList = snapshot.toObjects<Product>()
        }
    }

    val filteredProducts = productList.filter { it.title.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary).padding(bottom = 8.dp)) {
                // Header Lokasi
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                    Text(
                        "Jakarta Selatan",
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
                    )
                )
            }
        },
        floatingActionButton = {
            // Tombol Jual yang menonjol
            ExtendedFloatingActionButton(
                onClick = onAddProductClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("JUAL") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Banner Carousel - Perbaikan span
            item(span = { GridItemSpan(2) }) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    ) {
                        Text(
                            "PROMO AKHIR TAHUN",
                            modifier = Modifier.align(Alignment.Center),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // 2. Kategori Ikon
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

            // 3. Grid Produk
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
        modifier = Modifier.padding(8.dp).fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box {
                // Decode Base64 untuk menampilkan gambar tanpa Storage
                val bitmap = remember(product.images) {
                    try {
                        val imageString = product.images.firstOrNull()
                        if (!imageString.isNullOrEmpty()) {
                            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        } else null
                    } catch (e: Exception) { null }
                }

                AsyncImage(
                    model = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentScale = ContentScale.Crop
                )

                // Badge Kondisi
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(4.dp).clip(RoundedCornerShape(4.dp))
                ) {
                    Text(
                        product.condition,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(4.dp, 2.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Rp ${product.price}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(text = product.title, maxLines = 1, fontSize = 14.sp)
                Text(
                    text = "📍 ${product.city_location}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}