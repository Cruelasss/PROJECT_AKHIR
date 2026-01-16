package com.example.project_akhir.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.project_akhir.model.Product
import com.google.firebase.firestore.FirebaseFirestore

import kotlin.io.encoding.ExperimentalEncodingApi
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // State untuk Form
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Bekas") }
    var category by remember { mutableStateOf("Elektronik") }
    var location by remember { mutableStateOf("DIY Yogyakarta") }
    var imageList by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }

    // --- FITUR BARU: Launcher Galeri ---
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newImages = uris.mapNotNull { uri -> uriToBase64(context, uri) }
            imageList = newImages // Mengganti dengan foto baru yang dipilih
        }
    }

    // Load Data Lama
    LaunchedEffect(productId) {
        db.collection("products").document(productId).get().addOnSuccessListener { snapshot ->
            val product = snapshot.toObject(Product::class.java)
            product?.let {
                title = it.title
                price = it.price.toLong().toString()
                description = it.description
                condition = it.condition
                category = it.category
                location = it.city_location
                imageList = it.images
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Barang") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Grid Foto dengan Tombol Tambah
            Text("Foto Barang (Klik + untuk Ganti)", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Ambil Foto Baru
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { launcher.launch("image/*") },
                    shape = RoundedCornerShape(8.dp),
                    color = Color.LightGray
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(30.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                LazyRow {
                    items(imageList) { img ->
                        val bitmap = remember(img) {
                            try {
                                val imageBytes = Base64.decode(img, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            } catch (e: Exception) { null }
                        }
                        Box(modifier = Modifier.padding(4.dp)) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // Tombol Hapus Foto Satuan
                            IconButton(
                                onClick = { imageList = imageList.filter { it != img } },
                                modifier = Modifier.size(24.dp).align(Alignment.TopEnd).background(Color.White, CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 2. Input Field
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Iklan") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Harga (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // 3. Dropdown Kategori & Lokasi
            Spacer(modifier = Modifier.height(16.dp))
            Text("Kategori & Lokasi", fontWeight = FontWeight.Bold)

            CategoryDropdown(selected = category, onSelected = { category = it })
            ProvinceDropdown(selected = location, onSelected = { location = it })

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Tombol Simpan Perubahan (Ditambah Update Foto)
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = {
                    if (imageList.isEmpty()) {
                        Toast.makeText(context, "Minimal harus ada 1 foto!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    val updatedData = mapOf(
                        "title" to title,
                        "price" to (price.toDoubleOrNull() ?: 0.0),
                        "description" to description,
                        "category" to category,
                        "city_location" to location,
                        "condition" to condition,
                        "images" to imageList // Update foto baru ke Firebase
                    )

                    db.collection("products").document(productId)
                        .update(updatedData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Gagal update!", Toast.LENGTH_SHORT).show()
                        }
                }
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Simpan Perubahan")
            }
        }
    }
}

// --- FUNGSI HELPER CONVERT URI KE BASE64 ---
fun uriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) Base64.encodeToString(bytes, Base64.DEFAULT) else null
    } catch (e: Exception) {
        null
    }
}

// Dropdown Kategori & Provinsi tetap sama sesuai request Anda
@Composable
fun ProvinceDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val provinces = listOf( "Aceh", "Bali", "Banten", "Bengkulu", "DIY Yogyakarta",
        "DKI Jakarta", "Gorontalo", "Jambi", "Jawa Barat", "Jawa Tengah", "Jawa Timur",
        "Kalimantan Barat", "Kalimantan Selatan", "Kalimantan Tengah", "Kalimantan Timur",
        "Kalimantan Utara", "Kepulauan Bangka Belitung", "Kepulauan Riau", "Lampung",
        "Maluku", "Maluku Utara", "Nusa Tenggara Barat", "Nusa Tenggara Timur", "Papua",
        "Riau", "Sulawesi Barat", "Sulawesi Selatan", "Sulawesi Tengah", "Sulawesi Tenggara",
        "Sulawesi Utara", "Sumatera Barat", "Sumatera Selatan", "Sumatera Utara")

    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Lokasi: $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            provinces.forEach { p ->
                DropdownMenuItem(text = { Text(p) }, onClick = { onSelected(p); expanded = false })
            }
        }
    }
}

@Composable
fun CategoryDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val cats = listOf("Elektronik", "Mobil", "Motor", "Properti", "Hobi", "Jasa")

    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Kategori: $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cats.forEach { c ->
                DropdownMenuItem(text = { Text(c) }, onClick = { onSelected(c); expanded = false })
            }
        }
    }
}