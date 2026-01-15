package com.example.project_akhir.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.project_akhir.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(onSuccess: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Bekas") }
    var imageBase64 by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Launcher untuk tetap bisa pilih file dari Explorer
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Ubah gambar jadi Base64 agar tidak butuh Firebase Storage
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Kompres gambar agar tidak melebihi limit Firestore (1MB)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val byteArray = outputStream.toByteArray()
            imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            Toast.makeText(context, "Foto berhasil diproses", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Pasang Iklan (Tanpa Storage)") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            Button(onClick = { launcher.launch("image/*") }) {
                Text(if (imageBase64.isEmpty()) "Pilih Foto dari HP" else "Foto Siap di-Upload")
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga") }, modifier = Modifier.fillMaxWidth())

            // Pilihan Kondisi sesuai RAT
            Text("Kondisi:", modifier = Modifier.padding(top = 8.dp))
            Row {
                listOf("Baru", "Bekas").forEach { item ->
                    RadioButton(selected = (condition == item), onClick = { condition = item })
                    Text(item, modifier = Modifier.padding(top = 12.dp, end = 8.dp))
                }
            }

            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth().height(100.dp))

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (imageBase64.isEmpty()) {
                            Toast.makeText(context, "Pilih foto dulu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        val productId = db.collection("products").document().id
                        val product = Product(
                            productId = productId,
                            sellerId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            title = title,
                            price = price.toDoubleOrNull() ?: 0.0,
                            city_location = location,
                            description = description,
                            condition = condition,
                            images = listOf(imageBase64) // Simpan teks Base64 ke Firestore
                        )

                        db.collection("products").document(productId).set(product)
                            .addOnSuccessListener {
                                isLoading = false
                                onSuccess()
                            }
                    }
                ) {
                    Text("Pasang Iklan Sekarang")
                }
            }
        }
    }
}