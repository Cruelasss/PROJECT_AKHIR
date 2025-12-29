package com.example.project_akhir.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    // CoroutineScope diperlukan untuk menjalankan animasi scroll
    val scope = rememberCoroutineScope()

    val onboardPages = listOf(
        Triple("Temukan Barang Impian", "Cari berbagai barang bekas berkualitas di sekitar Anda.", Icons.Default.Search),
        Triple("Jual Cepat", "Pasang iklan hanya dalam beberapa detik langsung dari HP.", Icons.Default.AddCircle),
        Triple("Chat Langsung", "Negosiasi aman dan cepat melalui integrasi WhatsApp.", Icons.Default.Share)
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    onboardPages[page].third,
                    contentDescription = null,
                    modifier = Modifier.size(200.dp),
                    tint = Color(0xFF002F34)
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = onboardPages[page].first,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF002F34)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = onboardPages[page].second,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indikator Titik
            Row {
                repeat(3) { index ->
                    val color = if (pagerState.currentPage == index) Color(0xFF002F34) else Color.LightGray
                    Box(modifier = Modifier.padding(4.dp).size(10.dp).clip(CircleShape).background(color))
                }
            }

            // PERBAIKAN TOMBOL LANJUT
            Button(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage < 2) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            onFinish()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002F34))
            ) {
                Text(if (pagerState.currentPage == 2) "Mulai" else "Lanjut")
            }
        }
    }
}