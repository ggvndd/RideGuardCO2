package com.capstoneco2.rideguard.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing RideGuard hardware device connections
 * Handles hardware-user relationships and device switching logic
 */
@Singleton
class RideGuardIdService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("rideguard_id")

    /**
     * Connect a user to a RideGuard device
     * Handles switching between users if device is already connected
     */
    suspend fun connectUserToDevice(rideGuardId: String, userId: String): Result<Unit> {
        return try {
            val existingConnection = getRideGuardConnection(rideGuardId)
            
            if (existingConnection != null) {
                // Device exists, update connection
                val updatedConnection = existingConnection.copy(
                    previousUserId = existingConnection.currentUserId,
                    currentUserId = userId,
                    connectedAt = System.currentTimeMillis(),
                    lastActiveAt = System.currentTimeMillis()
                )
                
                collection.document(rideGuardId)
                    .set(updatedConnection.toMap())
                    .await()
            } else {
                // New device connection
                val newConnection = RideGuardId(
                    id = rideGuardId,
                    currentUserId = userId,
                    connectedAt = System.currentTimeMillis(),
                    lastActiveAt = System.currentTimeMillis()
                )
                
                collection.document(rideGuardId)
                    .set(newConnection.toMap())
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the current connection for a RideGuard device
     */
    suspend fun getRideGuardConnection(rideGuardId: String): RideGuardId? {
        return try {
            val document = collection.document(rideGuardId).get().await()
            if (document.exists()) {
                document.toObject(RideGuardId::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get all devices connected to a specific user
     */
    suspend fun getUserDevices(userId: String): List<RideGuardId> {
        return try {
            val query = collection.whereEqualTo("currentUserId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            query.documents.mapNotNull { document ->
                document.toObject(RideGuardId::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Disconnect a device (deactivate)
     */
    suspend fun disconnectDevice(rideGuardId: String): Result<Unit> {
        return try {
            collection.document(rideGuardId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update device last active time
     */
    suspend fun updateLastActive(rideGuardId: String): Result<Unit> {
        return try {
            collection.document(rideGuardId)
                .update("lastActiveAt", System.currentTimeMillis())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}