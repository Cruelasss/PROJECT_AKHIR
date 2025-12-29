package com.example.project_akhir.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project_akhir.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var userData by remember { mutableStateOf<User?>(null) }

    // Ambil data profil dari Firestore
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
                userData = snapshot.toObject(User::class.java)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil Saya") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userData != null) {
                Text(text = userData!!.full_name, style = MaterialTheme.typography.headlineMedium) //
                Text(text = userData!!.email, style = MaterialTheme.typography.bodyLarge) //
                Text(text = "WA: ${userData!!.phone_number}", style = MaterialTheme.typography.bodyMedium) //

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        auth.signOut() // Proses Logout Firebase
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Keluar Akun (Logout)")
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}