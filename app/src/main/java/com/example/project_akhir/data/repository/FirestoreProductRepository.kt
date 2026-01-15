package com.example.project_akhir.data.repository



import com.example.project_akhir.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProductRepository(
    private val firestore: FirebaseFirestore
)
    : ProductRepository {
    private val productCollection = firestore.collection("products")

    override fun getAllProducts(): Flow<List<Product>> = callbackFlow {
        val subscription = productCollection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val products = snapshot.toObjects(Product::class.java)
                trySend(products)
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getProductById(id: String): Flow<Product?> = callbackFlow {
        val subscription = productCollection.document(id).addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val product = snapshot.toObject(Product::class.java)
                trySend(product)
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveProduct(product: Product) {
        // Jika ID kosong, Firestore akan otomatis membuat ID baru
        val documentRef = if (product.productId.isEmpty()) {
            productCollection.document()
        } else {
            productCollection.document(product.productId)
        }

        val newProduct = product.copy(productId = documentRef.id)
        documentRef.set(newProduct).await()
    }

    override suspend fun updateProduct(product: Product) {
        productCollection.document(product.productId).set(product).await()
    }

    override suspend fun deleteProduct(id: String) {
        productCollection.document(id).delete().await()
    }
}