package com.example.project_akhir.model

data class User(
    val uid: String = "",
    val email: String = "",
    val full_name: String = "",
    val phone_number: String = "", // Pastikan sama persis dengan nama di Firestore
    val is_verified: Boolean = false
)