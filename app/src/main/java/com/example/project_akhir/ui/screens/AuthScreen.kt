package com.example.project_akhir.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) { // Perbaikan: Menambahkan parameter navigasi
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegisterMode) "Daftar BekasinAja" else "Login BekasinAja",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isRegisterMode) {
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Nomor WA (628...)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Email dan Password harus diisi", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    if (isRegisterMode) {
                        // LOGIKA DAFTAR
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { result ->
                                val uid = result.user?.uid
                                val userData = hashMapOf(
                                    "uid" to uid,
                                    "full_name" to fullName,
                                    "email" to email,
                                    "phone_number" to phoneNumber,
                                    "created_at" to System.currentTimeMillis()
                                )
                                // Simpan ke Firestore koleksi "users"
                                db.collection("users").document(uid!!).set(userData)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        Toast.makeText(context, "Daftar Berhasil!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess() // Pindah ke HomeScreen
                                    }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        // LOGIKA LOGIN
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                isLoading = false
                                Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess() // Pindah ke HomeScreen
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Login Gagal: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRegisterMode) "Daftar Sekarang" else "Masuk")
            }
        }

        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
            // Perbaikan baris 107: Menambahkan else branch
            Text(
                text = if (isRegisterMode) "Sudah punya akun? Login" else "Belum punya akun? Daftar"
            )
        }
    }
}