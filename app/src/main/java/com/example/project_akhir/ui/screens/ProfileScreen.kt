package com.example.project_akhir.view.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.project_akhir.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit // Fungsi navigasi ke halaman edit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var userData by remember { mutableStateOf<User?>(null) }
    val currentUserId = auth.currentUser?.uid ?: ""

    // Ambil data profil terbaru
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            db.collection("users").document(currentUserId).addSnapshotListener { snapshot, _ ->
                userData = snapshot?.toObject(User::class.java)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Akun Saya") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto Profil
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.LightGray
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(20.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userData?.full_name ?: "Memuat...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(text = userData?.email ?: "", color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Edit Profil (Sekarang Bisa Di-klik)
            Button(
                onClick = onEditProfileClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002F34)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Lihat dan edit profil", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Menu Profile yang Disederhanakan
            SimpleProfileItem(
                icon = Icons.Default.Settings,
                title = "Pengaturan Akun",
                onClick = onEditProfileClick
            )

            SimpleProfileItem(
                icon = Icons.Default.Info,
                title = "Hubungi Developer",
                onClick = {
                    // Mengarahkan langsung ke WhatsApp/Profil Developer
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/62895410183783"))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun SimpleProfileItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color(0xFF002F34))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
    }
}