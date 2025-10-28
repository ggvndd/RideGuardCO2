package com.capstoneco2.rideguard.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing hospital locations and emergency services
 * Provides hospital information for crash incident responses
 */
@Singleton
class HospitalService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("hospital")



    /**
     * Get all active hospitals
     */
    suspend fun getAllHospitals(): List<Hospital> {
        return try {
            val query = collection
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()
            
            query.documents.mapNotNull { document ->
                document.toObject(Hospital::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}