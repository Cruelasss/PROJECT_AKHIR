package com.example.project_akhir.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
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
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header ala OLX
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { /* Back Action */ }) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
            TextButton(onClick = { /* Help Action */ }) {
                Text("Bantuan", fontWeight = FontWeight.Bold, color = Color(0xFF002F34))
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // Branding
        Icon(Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFF002F34))
        Text(
            text = "BekasinAja",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF002F34)
        )
        Text("INDONESIA", style = MaterialTheme.typography.labelLarge, letterSpacing = 4.sp)

        Spacer(modifier = Modifier.weight(0.15f))

        // Form Fields
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isRegisterMode) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Nomor WA (628...)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF002F34))
        } else {
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Lengkapi data Anda", Toast.LENGTH_SHORT).show()
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
                                db.collection("users").document(uid!!).set(userData)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onLoginSuccess()
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
                                onLoginSuccess()
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Login Gagal: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002F34)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isRegisterMode) "Daftar Sekarang" else "Masuk ke Akun", fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
            Text(
                text = if (isRegisterMode) "Sudah punya akun? Masuk" else "Belum punya akun? Buat Akun",
                color = Color(0xFF002F34),
                fontWeight = FontWeight.Bold
            )
        }
    }
}