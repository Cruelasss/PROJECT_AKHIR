package com.example.project_akhir.model

// Simpan di package com.example.project_akhir.model
data class User(
    val uid: String = "",          // Menggantikan user_id UUID
    val email: String = "",        //
    val full_name: String = "",    //
    val phone_number: String = "", //
    val is_verified: Boolean = false //
)