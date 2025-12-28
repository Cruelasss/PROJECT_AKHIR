package com.example.project_akhir

import android.os.Bundle
import android.util.Log // Tambahkan ini untuk melihat hasil di Logcat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.project_akhir.ui.theme.PROJECT_AKHIRTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- KODE TES FIREBASE MULAI ---
        val db = Firebase.firestore
        val testData = hashMapOf(
            "full_name" to "Tes Koneksi BekasinAja",
            "status" to "Berhasil Tersambung!"
        )

        db.collection("connection_test")
            .add(testData)
            .addOnSuccessListener { documentReference ->
                Log.d("FIREBASE_OK", "Data berhasil masuk! ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_ERROR", "Gagal koneksi: ", e)
            }
        // --- KODE TES FIREBASE SELESAI ---

        enableEdgeToEdge()
        setContent {
            PROJECT_AKHIRTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Firebase Test Running...",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Status: $name!", modifier = modifier)
}