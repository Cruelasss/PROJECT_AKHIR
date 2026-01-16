package com.example.project_akhir.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.project_akhir.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(onSuccess: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // State sesuai field di database terbaru
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Bekas") }
    var category by remember { mutableStateOf("Elektronik") }
    var location by remember { mutableStateOf("DIY Yogyakarta") }
    var whatsappNumber by remember { mutableStateOf("") }
    var showWhatsappInfo by remember { mutableStateOf(true) }

    // State untuk Multiple Photos (List of Base64)
    var imageList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Launcher untuk pilih BANYAK foto sekaligus
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            if (imageList.size + uris.size > 5) {
                Toast.makeText(context, "Maksimal 5 foto", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }

            val processedImages = mutableListOf<String>()
            uris.forEach { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    // Kompresi agar ukuran tidak terlalu besar
                    val outputStream = ByteArrayOutputStream()
                    // Quality 70% untuk menghemat space
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val byteArray = outputStream.toByteArray()
                    val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
                    processedImages.add(base64String)
                } catch (e: Exception) {
                    // Skip error
                }
            }
            imageList = imageList + processedImages
            Toast.makeText(context, "${uris.size} foto ditambahkan", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk menghapus foto
    fun removeImage(index: Int) {
        imageList = imageList.toMutableList().apply { removeAt(index) }
    }

    // Fungsi validasi nomor WhatsApp
    fun isValidWhatsAppNumber(number: String): Boolean {
        if (number.isBlank()) return false
        val cleanNumber = number.trim()
            .replace(Regex("[^0-9+]"), "")
        return cleanNumber.length >= 10 && cleanNumber.length <= 15
    }

    // Format nomor WhatsApp untuk ditampilkan
    fun formatWhatsAppNumber(number: String): String {
        if (number.isBlank()) return ""
        val cleanNumber = number.trim()
            .replace(Regex("[^0-9+]"), "")

        return when {
            cleanNumber.startsWith("+62") -> cleanNumber
            cleanNumber.startsWith("62") -> "+$cleanNumber"
            cleanNumber.startsWith("0") -> "+62${cleanNumber.substring(1)}"
            cleanNumber.isNotEmpty() -> "+62$cleanNumber"
            else -> ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pasang Iklan Baru") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // 1. Bagian Upload Foto
            Text(
                text = "Foto Barang (Maksimal 5 foto):",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    enabled = imageList.size < 5
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih Foto")
                }

                if (imageList.isNotEmpty()) {
                    Button(
                        onClick = { imageList = emptyList() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Hapus Semua")
                    }
                }
            }

            // Info jumlah foto
            if (imageList.isNotEmpty()) {
                Text(
                    text = "${imageList.size}/5 foto terpilih",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Preview foto kecil (LazyRow)
            if (imageList.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(imageList) { index, img ->
                        Box {
                            // Dekode gambar di dalam composable
                            val bitmap = remember(img) {
                                try {
                                    val bytes = Base64.decode(img, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Foto $index",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Error", color = MaterialTheme.colorScheme.error)
                                }
                            }

                            // Tombol hapus di pojok kanan atas
                            IconButton(
                                onClick = { removeImage(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus foto",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Input Data Barang
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nama Barang *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = title.isEmpty()
            )

            if (title.isEmpty()) {
                Text(
                    text = "Nama barang wajib diisi",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            OutlinedTextField(
                value = price,
                onValueChange = {
                    // Hanya terima angka
                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                        price = it
                    }
                },
                label = { Text("Harga (Rp) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                trailingIcon = {
                    if (price.isNotEmpty()) {
                        val formattedPrice = try {
                            NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                                .format(price.toDoubleOrNull() ?: 0)
                        } catch (e: Exception) {
                            ""
                        }
                        if (formattedPrice.isNotEmpty()) {
                            Text(
                                text = formattedPrice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                },
                isError = price.isEmpty() || (price.toDoubleOrNull() ?: 0.0) <= 0
            )

            if (price.isEmpty() || (price.toDoubleOrNull() ?: 0.0) <= 0) {
                Text(
                    text = if (price.isEmpty()) "Harga wajib diisi" else "Harga harus lebih dari 0",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown Kategori
            CategoryDropdown(
                selected = category,
                onSelected = { category = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown Lokasi/Provinsi
            ProvinceDropdown(
                selected = location,
                onSelected = { location = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Input Nomor WhatsApp
            if (showWhatsappInfo) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nomor WhatsApp",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pembeli akan menghubungi Anda via WhatsApp untuk bertransaksi",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        IconButton(
                            onClick = { showWhatsappInfo = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup info")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = whatsappNumber,
                onValueChange = { whatsappNumber = it },
                label = { Text("Nomor WhatsApp Penjual *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contoh: 081234567890") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                trailingIcon = {
                    if (whatsappNumber.isNotBlank() && isValidWhatsAppNumber(whatsappNumber)) {
                        val formattedNumber = formatWhatsAppNumber(whatsappNumber)
                        Text(
                            text = formattedNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                },
                isError = whatsappNumber.isNotBlank() && !isValidWhatsAppNumber(whatsappNumber)
            )

            if (whatsappNumber.isNotBlank() && !isValidWhatsAppNumber(whatsappNumber)) {
                Text(
                    text = "Format nomor tidak valid (contoh: 081234567890)",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pilihan Kondisi
            Text(
                text = "Kondisi:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf("Baru", "Bekas").forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { condition = item }
                    ) {
                        RadioButton(
                            selected = (condition == item),
                            onClick = { condition = item }
                        )
                        Text(
                            text = item,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Deskripsi
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi Lengkap") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                singleLine = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Tombol Simpan
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val user = auth.currentUser
                        if (user == null) {
                            Toast.makeText(context, "Harus login dulu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Validasi input
                        if (title.isEmpty()) {
                            Toast.makeText(context, "Nama barang wajib diisi", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val priceValue = price.toDoubleOrNull() ?: 0.0
                        if (priceValue <= 0) {
                            Toast.makeText(context, "Harga harus lebih dari 0", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (!isValidWhatsAppNumber(whatsappNumber)) {
                            Toast.makeText(
                                context,
                                "Nomor WhatsApp tidak valid. Contoh: 081234567890",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (imageList.isEmpty()) {
                            Toast.makeText(context, "Minimal 1 foto diperlukan", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true

                        // Format nomor WhatsApp untuk disimpan
                        val formattedWhatsapp = formatWhatsAppNumber(whatsappNumber)

                        // Ambil data user untuk sellerName
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { uSnap ->
                                val sellerName = uSnap.getString("full_name") ?: "Penjual"

                                // Update nomor WhatsApp di profil user juga
                                val userUpdate = hashMapOf<String, Any>(
                                    "phone_number" to formattedWhatsapp
                                )

                                db.collection("users").document(user.uid)
                                    .update(userUpdate)
                                    .addOnSuccessListener {
                                        // Lanjutkan membuat produk
                                        val productId = db.collection("products").document().id
                                        val product = Product(
                                            productId = productId,
                                            sellerId = user.uid,
                                            sellerName = sellerName,
                                            title = title,
                                            description = description,
                                            price = priceValue,
                                            condition = condition,
                                            category = category,
                                            city_location = location,
                                            createdAt = System.currentTimeMillis(),
                                            images = imageList,
                                            // Simpan juga nomor WhatsApp di produk untuk akses cepat
                                            whatsappNumber = formattedWhatsapp
                                        )

                                        db.collection("products").document(productId).set(product)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Iklan berhasil dipasang!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onSuccess()
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Gagal upload produk: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Gagal menyimpan nomor WhatsApp: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Gagal mengambil data user: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    },
                    enabled = !isLoading
                ) {
                    Text("Pasang Iklan Sekarang")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Elektronik",
        "Kendaraan",
        "Fashion",
        "Rumah Tangga",
        "Hobi & Olahraga",
        "Jasa",
        "Lainnya"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategori *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinceDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val provinces = listOf(
        "Aceh",
        "Sumatera Utara",
        "Sumatera Barat",
        "Riau",
        "Kepulauan Riau",
        "Jambi",
        "Sumatera Selatan",
        "Bangka Belitung",
        "Bengkulu",
        "Lampung",
        "Banten",
        "DKI Jakarta",
        "Jawa Barat",
        "Jawa Tengah",
        "DIY Yogyakarta",
        "Jawa Timur",
        "Bali",
        "Nusa Tenggara Barat",
        "Nusa Tenggara Timur",
        "Kalimantan Barat",
        "Kalimantan Tengah",
        "Kalimantan Selatan",
        "Kalimantan Timur",
        "Kalimantan Utara",
        "Sulawesi Utara",
        "Gorontalo",
        "Sulawesi Tengah",
        "Sulawesi Selatan",
        "Sulawesi Tenggara",
        "Maluku",
        "Maluku Utara",
        "Papua Barat",
        "Papua"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Lokasi/Provinsi *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            provinces.forEach { province ->
                DropdownMenuItem(
                    text = { Text(province) },
                    onClick = {
                        onSelected(province)
                        expanded = false
                    }
                )
            }
        }
    }
}