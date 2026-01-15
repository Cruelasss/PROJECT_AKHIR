package com.example.project_akhir.model


data class Product(
    val productId: String = "",
    val sellerId: String = "",
    val sellerName: String = "", // Tambahan: Menampilkan siapa yang upload
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val condition: String = "",
    val category: String = "",   // Tambahan: Elektronik, Kendaraan, dll
    val city_location: String = "",   // Tambahan: Provinsi
    val createdAt: Long = System.currentTimeMillis(), // Tambahan: Tanggal posting
    val images: List<String> = emptyList() // Sudah mendukung banyak foto
)