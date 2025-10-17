package com.capstoneco2.rideguard.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

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
     * Add a new hospital to the system
     */
    suspend fun addHospital(
        name: String,
        latitude: Double,
        longitude: Double,
        address: String? = null,
        phoneNumber: String? = null,
        emergencyPhone: String? = null,
        capacity: Int? = null
    ): Result<Hospital> {
        return try {
            val hospitalId = collection.document().id
            val hospital = Hospital(
                id = hospitalId,
                name = name,
                latitude = latitude,
                longitude = longitude,
                address = address,
                phoneNumber = phoneNumber,
                emergencyPhone = emergencyPhone,
                capacity = capacity,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            collection.document(hospitalId)
                .set(hospital.toMap())
                .await()
            
            Result.success(hospital)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get hospital by ID
     */
    suspend fun getHospitalById(hospitalId: String): Hospital? {
        return try {
            val document = collection.document(hospitalId).get().await()
            if (document.exists()) {
                document.toObject(Hospital::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

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

    /**
     * Find nearest hospitals to a crash location
     * Uses Haversine formula for distance calculation
     */
    suspend fun getNearestHospitals(
        crashLatitude: Double,
        crashLongitude: Double,
        maxDistance: Double = 50.0, // Maximum distance in kilometers
        maxResults: Int = 5
    ): List<Pair<Hospital, Double>> {
        return try {
            val allHospitals = getAllHospitals()
            
            val hospitalsWithDistance = allHospitals.map { hospital ->
                val distance = calculateDistance(
                    crashLatitude, crashLongitude,
                    hospital.latitude, hospital.longitude
                )
                Pair(hospital, distance)
            }
            
            // Filter by max distance and sort by distance
            hospitalsWithDistance
                .filter { it.second <= maxDistance }
                .sortedBy { it.second }
                .take(maxResults)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Update hospital information
     */
    suspend fun updateHospital(
        hospitalId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            val updateData = updates.toMutableMap()
            updateData["updatedAt"] = System.currentTimeMillis()
            
            collection.document(hospitalId)
                .update(updateData)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deactivate a hospital
     */
    suspend fun deactivateHospital(hospitalId: String): Result<Unit> {
        return try {
            collection.document(hospitalId)
                .update(mapOf(
                    "isActive" to false,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search hospitals by name
     */
    suspend fun searchHospitalsByName(query: String): List<Hospital> {
        return try {
            // Since Firestore doesn't support full-text search, we'll get all hospitals
            // and filter client-side. For production, consider using Algolia or similar.
            val allHospitals = getAllHospitals()
            
            allHospitals.filter { hospital ->
                hospital.name.contains(query, ignoreCase = true) ||
                hospital.address?.contains(query, ignoreCase = true) == true
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get hospitals within a geographic bounding box
     */
    suspend fun getHospitalsInBounds(
        northLatitude: Double,
        southLatitude: Double,
        eastLongitude: Double,
        westLongitude: Double
    ): List<Hospital> {
        return try {
            val allHospitals = getAllHospitals()
            
            allHospitals.filter { hospital ->
                hospital.latitude >= southLatitude &&
                hospital.latitude <= northLatitude &&
                hospital.longitude >= westLongitude &&
                hospital.longitude <= eastLongitude
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in kilometers
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0 // Earth's radius in kilometers
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return R * c
    }
}