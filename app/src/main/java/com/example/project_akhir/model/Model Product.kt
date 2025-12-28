package com.example.project_akhir.model


data class Product(
    val productId: String = "",    // Menggantikan item_id
    val sellerId: String = "",     // Referensi ke User UID
    val title: String = "",        //
    val description: String = "",  //
    val price: Double = 0.0,       //
    val condition: String = "",    // Baru/Bekas/Rusak
    val city_location: String = "",//
    val images: List<String> = emptyList() // Menggantikan tabel Item_Images (C.3)
)