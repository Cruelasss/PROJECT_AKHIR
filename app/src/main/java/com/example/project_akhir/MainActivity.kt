package com.example.project_akhir


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.project_akhir.ui.NavGraph
import com.example.project_akhir.ui.theme.PROJECT_AKHIRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PROJECT_AKHIRTheme {
                // NavGraph sekarang mengontrol semua layar Anda
                NavGraph()
            }
        }
    }
}
