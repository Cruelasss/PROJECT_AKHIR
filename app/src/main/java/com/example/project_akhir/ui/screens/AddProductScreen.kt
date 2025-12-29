package com.example.project_akhir.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(onSuccess: () -> Unit) {
    val context = LocalContext.current

    // State Input sesuai Tabel Items (C.2) di RAT
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Bekas") } // ENUM: Baru, Bekas, Rusak

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Scaffold(topBar = { TopAppBar(title = { Text("Pasang Iklan BekasinAja") }) }) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
        ) {
            // Pemilihan File
            Button(onClick = { launcher.launch("*/*") }) {
                Text(if (imageUri == null) "Pilih File dari Explorer" else "File Terpilih")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Field: Title
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            // Field: Price
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (Rp)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            // Field: Condition (ENUM Baru, Bekas, Rusak)
            Text("Kondisi Barang:", style = MaterialTheme.typography.bodyMedium)
            val options = listOf("Baru", "Bekas", "Rusak")
            Row {
                options.forEach { text ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp).selectable(
                            selected = (condition == text),
                            onClick = { condition = text }
                        )
                    ) {
                        RadioButton(selected = (condition == text), onClick = { condition = text })
                        Text(text = text)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Field: City Location
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi (Kota)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            // Field: Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi Barang") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(progress = { uploadProgress }, modifier = Modifier.fillMaxWidth())
                    Text(text = "Mengunggah: ${(uploadProgress * 100).toInt()}%")
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (imageUri == null) {
                            Toast.makeText(context, "Pilih foto dulu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true

                        val storageRef = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")

                        // Memulai upload dengan Task Listener
                        val uploadTask = storageRef.putFile(imageUri!!)

                        uploadTask.addOnProgressListener { taskSnapshot ->
                            // Menghitung persentase
                            val progress = (1.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                            uploadProgress = progress.toFloat()
                        }.addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { url ->
                                // Simpan ke Firestore setelah upload selesai
                                val uid = FirebaseAuth.getInstance().currentUser?.uid
                                val db = FirebaseFirestore.getInstance()
                                val productId = db.collection("products").document().id

                                val product = Product(
                                    productId = productId,
                                    sellerId = uid ?: "",
                                    title = title,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    city_location = location,
                                    images = listOf(url.toString())
                                )

                                db.collection("products").document(productId).set(product)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onSuccess()
                                    }
                            }
                        }.addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Pasang Iklan Sekarang")
                }
            }
        }
    }
}