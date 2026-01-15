package com.example.project_akhir.data.repository


import com.example.project_akhir.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    // Mengambil semua data produk secara real-time
    fun getAllProducts(): Flow<List<Product>>

    // Mengambil satu data produk berdasarkan ID
    fun getProductById(id: String): Flow<Product?>

    // Menambah data produk baru
    suspend fun saveProduct(product: Product)

    // Memperbarui data produk
    suspend fun updateProduct(product: Product)

    // Menghapus data produk
    suspend fun deleteProduct(id: String)
}