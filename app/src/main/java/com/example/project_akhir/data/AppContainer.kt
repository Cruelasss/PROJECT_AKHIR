package com.example.project_akhir.data



import com.example.project_akhir.data.repository.FirestoreProductRepository
import com.example.project_akhir.data.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore

interface AppContainer {
    val productRepository: ProductRepository
}

class AppDataContainer : AppContainer {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override val productRepository: ProductRepository by lazy {
        FirestoreProductRepository(firestore)
    }
}