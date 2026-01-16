package com.example.project_akhir.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.project_akhir.model.Product
import com.example.project_akhir.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavHostController,
    onNavigateToEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var product by remember { mutableStateOf<Product?>(null) }
    var seller by remember { mutableStateOf<User?>(null) }
    var isLoadingSeller by remember { mutableStateOf(true) }
    var isFullScreen by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    var isFavorite by remember { mutableStateOf(false) }
    var showPhoneNumberDialog by remember { mutableStateOf(false) }

    // --- AMBIL NOMOR WHATSAPP YANG TERSEDIA ---
    val availableWhatsappNumber = remember(product, seller) {
        // Prioritaskan nomor dari produk (yang baru ditambahkan di AddProductScreen)
        product?.whatsappNumber?.takeIf { it.isNotBlank() }
            ?: seller?.phone_number?.takeIf { it.isNotBlank() }
            ?: ""
    }

    // Fungsi untuk refresh seller data
    fun refreshSellerData() {
        product?.sellerId?.let { sellerId ->
            isLoadingSeller = true
            db.collection("users").document(sellerId).get()
                .addOnSuccessListener { userSnapshot ->
                    if (userSnapshot.exists()) {
                        seller = userSnapshot.toObject(User::class.java)
                        Log.d("PRODUCT_DETAIL", "Seller data refreshed")
                    }
                    isLoadingSeller = false
                }
                .addOnFailureListener {
                    isLoadingSeller = false
                }
        }
    }

    // Fungsi untuk cek dan update nomor telepon
    fun checkAndUpdatePhoneNumber() {
        val isCurrentUserSeller = currentUser?.uid == seller?.uid
        val hasPhoneNumber = availableWhatsappNumber.isNotBlank()

        if (isCurrentUserSeller && !hasPhoneNumber) {
            // Jika user adalah penjual dan belum punya nomor
            showPhoneNumberDialog = true
        }
    }

    // --- 1. FETCH DATA PRODUK & SELLER ---
    LaunchedEffect(productId) {
        Log.d("PRODUCT_DETAIL", "Fetching product: $productId")

        // Fetch product data
        db.collection("products").document(productId).get()
            .addOnSuccessListener { productSnapshot ->
                val item = productSnapshot.toObject(Product::class.java)
                product = item
                Log.d("PRODUCT_DETAIL", "Product loaded: ${item?.title}")
                Log.d("PRODUCT_DETAIL", "Seller ID from product: ${item?.sellerId}")
                Log.d("PRODUCT_DETAIL", "Product WhatsApp: ${item?.whatsappNumber}") // <-- Debug

                // Fetch seller data
                item?.sellerId?.let { sellerId ->
                    Log.d("PRODUCT_DETAIL", "Fetching seller with ID: $sellerId")

                    db.collection("users").document(sellerId).get()
                        .addOnSuccessListener { userSnapshot ->
                            if (userSnapshot.exists()) {
                                seller = userSnapshot.toObject(User::class.java)
                                Log.d("PRODUCT_DETAIL", "Seller loaded: ${seller?.full_name}")
                                Log.d("PRODUCT_DETAIL", "Seller phone: ${seller?.phone_number}")

                                // Cek apakah penjual butuh update nomor
                                checkAndUpdatePhoneNumber()
                            } else {
                                Log.e("PRODUCT_DETAIL", "Seller not found with ID: $sellerId")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("PRODUCT_DETAIL", "Failed to fetch seller: ${e.message}")
                        }
                        .addOnCompleteListener {
                            isLoadingSeller = false
                        }
                } ?: run {
                    isLoadingSeller = false
                    Log.e("PRODUCT_DETAIL", "No sellerId found in product")
                }
            }
            .addOnFailureListener { e ->
                Log.e("PRODUCT_DETAIL", "Failed to fetch product: ${e.message}")
                isLoadingSeller = false
            }

        // Check favorite status
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).collection("favorites")
                .document(productId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("PRODUCT_DETAIL", "Error listening to favorite: ${error.message}")
                        return@addSnapshotListener
                    }
                    isFavorite = snapshot?.exists() == true
                }
        }
    }

    // --- 2. TOGGLE FAVORITE FUNCTION ---
    fun toggleFavorite() {
        if (currentUser == null) {
            navController.navigate("auth")
            return
        }

        val favRef = db.collection("users").document(currentUser.uid)
            .collection("favorites").document(productId)

        if (isFavorite) {
            favRef.delete()
                .addOnSuccessListener {
                    Log.d("PRODUCT_DETAIL", "Removed from favorites")
                }
                .addOnFailureListener { e ->
                    Log.e("PRODUCT_DETAIL", "Failed to remove favorite: ${e.message}")
                }
        } else {
            product?.let {
                favRef.set(it)
                    .addOnSuccessListener {
                        Log.d("PRODUCT_DETAIL", "Added to favorites")
                    }
                    .addOnFailureListener { e ->
                        Log.e("PRODUCT_DETAIL", "Failed to add favorite: ${e.message}")
                    }
            }
        }
    }

    // --- 3. WHATSAPP FUNCTION ---
    fun openWhatsApp(phoneNumber: String = availableWhatsappNumber) {
        if (currentUser == null) {
            navController.navigate("auth")
            return
        }

        Log.d("WHATSAPP", "Phone number to use: '$phoneNumber'")

        if (phoneNumber.isBlank()) {
            android.widget.Toast.makeText(
                context,
                "Nomor WhatsApp penjual tidak tersedia",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }

        // Clean and format phone number
        val cleanPhone = phoneNumber.trim()
            .replace(Regex("[^0-9+]"), "")
            .let {
                when {
                    it.startsWith("+62") -> it.substring(1) // +62812 -> 62812
                    it.startsWith("62") -> it // 62812
                    it.startsWith("0") -> "62${it.substring(1)}" // 0812 -> 62812
                    else -> "62$it" // 812 -> 62812
                }
            }

        Log.d("WHATSAPP", "Formatted phone: $cleanPhone")

        // Create message
        val productTitle = product?.title ?: "produk ini"
        val sellerName = seller?.full_name ?: "Admin"
        val message = "Halo $sellerName, saya tertarik dengan $productTitle yang Anda jual. Apakah masih tersedia?"
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val whatsappUrl = "https://wa.me/$cleanPhone?text=$encodedMessage"

        Log.d("WHATSAPP", "WhatsApp URL: $whatsappUrl")

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // WhatsApp not installed
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                context.startActivity(webIntent)
            } catch (e2: Exception) {
                android.widget.Toast.makeText(
                    context,
                    "WhatsApp tidak terinstall. Silakan install WhatsApp terlebih dahulu.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e("WHATSAPP", "Error opening WhatsApp: ${e.message}", e)
            android.widget.Toast.makeText(
                context,
                "Gagal membuka WhatsApp: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    // --- 4. DELETE PRODUCT FUNCTION ---
    fun deleteProduct() {
        db.collection("products").document(productId).delete()
            .addOnSuccessListener {
                Log.d("PRODUCT_DETAIL", "Product deleted successfully")
                android.widget.Toast.makeText(
                    context,
                    "Produk berhasil dihapus",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                onBack()
            }
            .addOnFailureListener { e ->
                Log.e("PRODUCT_DETAIL", "Failed to delete product: ${e.message}")
                android.widget.Toast.makeText(
                    context,
                    "Gagal menghapus produk",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
    }

    // --- 5. UPDATE PHONE NUMBER DIALOG ---
    if (showPhoneNumberDialog) {
        UpdatePhoneNumberDialog(
            onDismiss = { showPhoneNumberDialog = false },
            onUpdateSuccess = {
                showPhoneNumberDialog = false
                refreshSellerData()
                android.widget.Toast.makeText(
                    context,
                    "Nomor WhatsApp berhasil disimpan!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // --- 6. FULL SCREEN IMAGE DIALOG ---
    if (isFullScreen && product != null) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val pagerState = rememberPagerState(
                    initialPage = selectedImageIndex,
                    pageCount = { product!!.images.size }
                )

                HorizontalPager(state = pagerState) { index ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        val imageBitmap = remember(product!!.images[index]) {
                            try {
                                val bytes = Base64.decode(product!!.images[index], Base64.DEFAULT)
                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                bitmap?.asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "Full screen image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Gagal memuat gambar",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { isFullScreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Barang", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            product?.let { item ->
                val isOwner = currentUser?.uid == item.sellerId
                val hasWhatsAppNumber = availableWhatsappNumber.isNotBlank() // <-- PERUBAHAN DI SINI
                val isCurrentUserSeller = currentUser?.uid == seller?.uid

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isOwner) {
                                // Owner actions
                                Button(
                                    onClick = { onNavigateToEdit(item.productId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Edit Produk")
                                }

                                Button(
                                    onClick = { deleteProduct() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red.copy(alpha = 0.9f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Hapus")
                                }
                            } else {
                                // Buyer actions
                                val buttonText = when {
                                    currentUser == null -> "Login untuk WhatsApp"
                                    isLoadingSeller -> "Memuat info penjual..."
                                    !hasWhatsAppNumber && isCurrentUserSeller -> "Tambahkan Nomor WhatsApp"
                                    !hasWhatsAppNumber -> "Penjual tidak punya WhatsApp"
                                    else -> "Chat via WhatsApp"
                                }

                                val buttonEnabled = when {
                                    currentUser == null -> true
                                    isLoadingSeller -> false
                                    !hasWhatsAppNumber -> false // Nonaktif jika tidak ada nomor
                                    else -> true
                                }

                                val buttonColor = when {
                                    currentUser == null -> Color(0xFF25D366)
                                    !hasWhatsAppNumber && isCurrentUserSeller -> Color(0xFF2196F3) // Biru untuk edit
                                    !hasWhatsAppNumber -> Color.Gray
                                    else -> Color(0xFF25D366)
                                }

                                Button(
                                    onClick = {
                                        when {
                                            currentUser == null -> navController.navigate("auth")
                                            !hasWhatsAppNumber && isCurrentUserSeller -> {
                                                // Navigasi ke profile untuk edit nomor
                                                navController.navigate("profile")
                                            }
                                            !hasWhatsAppNumber -> {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Penjual belum menambahkan nomor WhatsApp",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            else -> openWhatsApp()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = buttonColor,
                                        contentColor = Color.White
                                    ),
                                    enabled = buttonEnabled
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                !hasWhatsAppNumber && isCurrentUserSeller -> Icons.Default.Edit
                                                else -> Icons.Default.Phone
                                            },
                                            contentDescription = "WhatsApp",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(buttonText)
                                    }
                                }
                            }
                        }

                        // Info tambahan untuk penjual yang belum punya nomor
                        if (isCurrentUserSeller && !hasWhatsAppNumber && !isLoadingSeller) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚠️",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Tambahkan Nomor WhatsApp",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Agar pembeli dapat menghubungi Anda",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Gallery
                product?.let { item ->
                    if (item.images.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { item.images.size })

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clickable {
                                    selectedImageIndex = pagerState.currentPage
                                    isFullScreen = true
                                }
                        ) {
                            HorizontalPager(state = pagerState) { index ->
                                Box(modifier = Modifier.fillMaxSize()) {
                                    val imageBitmap = remember(item.images[index]) {
                                        try {
                                            val bytes = Base64.decode(item.images[index], Base64.DEFAULT)
                                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            bitmap?.asImageBitmap()
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }

                                    if (imageBitmap != null) {
                                        Image(
                                            bitmap = imageBitmap,
                                            contentDescription = "Product image ${index + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.LightGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Gambar tidak tersedia")
                                        }
                                    }
                                }
                            }

                            // Image indicator
                            if (item.images.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(item.images.size) { index ->
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .size(8.dp)
                                                .background(
                                                    color = if (index == pagerState.currentPage)
                                                        Color.White else Color.White.copy(alpha = 0.5f),
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada gambar")
                        }
                    }
                }

                // Product Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    product?.let { item ->
                        // Title and Price
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                            .format(item.price)
                        Text(
                            text = formattedPrice,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category and Condition Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.category?.let { category ->
                                if (category.isNotBlank()) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(category) }
                                    )
                                }
                            }
                            item.condition?.let { condition ->
                                if (condition.isNotBlank()) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(condition) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Product Info Grid
                        ProductInfoItem(
                            icon = "📍",
                            label = "Lokasi",
                            value = item.city_location ?: "Tidak ditentukan"
                        )

                        ProductInfoItem(
                            icon = "👤",
                            label = "Penjual",
                            value = if (isLoadingSeller) "Memuat..."
                            else seller?.full_name ?: item.sellerName.ifEmpty { "Tidak diketahui" }
                        )

                        // Tampilkan nomor telepon jika tersedia
                        if (availableWhatsappNumber.isNotBlank()) { // <-- PERUBAHAN DI SINI
                            ProductInfoItem(
                                icon = "📱",
                                label = "Kontak WhatsApp",
                                value = availableWhatsappNumber
                            )
                        }

                        val dateStr = try {
                            SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
                                .format(Date(item.createdAt))
                        } catch (e: Exception) {
                            "Baru saja"
                        }
                        ProductInfoItem(icon = "📅", label = "Diposting", value = dateStr)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Description
                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Deskripsi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.description ?: "Tidak ada deskripsi",
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = androidx.compose.ui.unit.TextUnit(20f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductInfoItem(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePhoneNumberDialog(
    onDismiss: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tambahkan Nomor WhatsApp", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Untuk memudahkan pembeli menghubungi Anda, silakan tambahkan nomor WhatsApp Anda.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Nomor WhatsApp (contoh: 081234567890)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = phoneNumber.isNotBlank() && !isValidPhoneNumber(phoneNumber)
                )
                if (phoneNumber.isNotBlank() && !isValidPhoneNumber(phoneNumber)) {
                    Text(
                        "Format nomor tidak valid",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isValidPhoneNumber(phoneNumber)) {
                        android.widget.Toast.makeText(context, "Format nomor tidak valid", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    currentUser?.uid?.let { uid ->
                        db.collection("users").document(uid)
                            .update("phone_number", phoneNumber)
                            .addOnSuccessListener {
                                isLoading = false
                                onUpdateSuccess()
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                android.widget.Toast.makeText(
                                    context,
                                    "Gagal menyimpan: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                },
                enabled = !isLoading && isValidPhoneNumber(phoneNumber)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Nanti Saja")
            }
        }
    )
}

// Fungsi validasi nomor telepon
fun isValidPhoneNumber(phone: String): Boolean {
    if (phone.isBlank()) return false
    val cleanPhone = phone.trim()
        .replace(Regex("[^0-9+]"), "")
    return cleanPhone.length >= 10 && cleanPhone.length <= 15
}