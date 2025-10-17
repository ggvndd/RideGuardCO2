package com.capstoneco2.rideguard.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing authentication tokens for user-hardware relationships
 * Allows users to have multiple hardware devices with proper authentication
 */
@Singleton
class TokenService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("token")

    /**
     * Create a new token for user-hardware authentication
     */
    suspend fun createToken(
        tokenId: String,
        userId: String,
        rideGuardId: String,
        deviceInfo: String,
        fcmToken: String? = null,
        expiresAt: Long? = null
    ): Result<Token> {
        return try {
            val token = Token(
                tokenId = tokenId,
                userId = userId,
                rideGuardId = rideGuardId,
                deviceInfo = deviceInfo,
                fcmToken = fcmToken,
                createdAt = System.currentTimeMillis(),
                lastUsedAt = System.currentTimeMillis(),
                expiresAt = expiresAt,
                isActive = true
            )
            
            collection.document(tokenId)
                .set(token.toMap())
                .await()
            
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate a token and update last used time
     */
    suspend fun validateToken(tokenId: String): Result<Token?> {
        return try {
            val document = collection.document(tokenId).get().await()
            
            if (document.exists()) {
                val token = document.toObject(Token::class.java)
                
                if (token != null && token.isActive) {
                    // Check if token has expired
                    if (token.expiresAt != null && token.expiresAt < System.currentTimeMillis()) {
                        // Token expired, deactivate it
                        collection.document(tokenId)
                            .update("isActive", false)
                            .await()
                        Result.success(null)
                    } else {
                        // Token is valid, update last used time
                        collection.document(tokenId)
                            .update("lastUsedAt", System.currentTimeMillis())
                            .await()
                        Result.success(token)
                    }
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all tokens for a specific user
     */
    suspend fun getUserTokens(userId: String): List<Token> {
        return try {
            val query = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .orderBy("lastUsedAt")
                .get()
                .await()
            
            query.documents.mapNotNull { document ->
                document.toObject(Token::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get all tokens for a specific RideGuard device
     */
    suspend fun getDeviceTokens(rideGuardId: String): List<Token> {
        return try {
            val query = collection
                .whereEqualTo("rideGuardId", rideGuardId)
                .whereEqualTo("isActive", true)
                .orderBy("lastUsedAt")
                .get()
                .await()
            
            query.documents.mapNotNull { document ->
                document.toObject(Token::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Revoke a token (deactivate)
     */
    suspend fun revokeToken(tokenId: String): Result<Unit> {
        return try {
            collection.document(tokenId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Revoke all tokens for a user
     */
    suspend fun revokeAllUserTokens(userId: String): Result<Unit> {
        return try {
            val userTokens = getUserTokens(userId)
            
            // Use batch write for efficiency
            val batch = firestore.batch()
            userTokens.forEach { token ->
                val tokenDoc = collection.document(token.tokenId)
                batch.update(tokenDoc, "isActive", false)
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update FCM token for push notifications
     */
    suspend fun updateFcmToken(tokenId: String, fcmToken: String): Result<Unit> {
        return try {
            collection.document(tokenId)
                .update("fcmToken", fcmToken)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clean up expired tokens
     */
    suspend fun cleanupExpiredTokens(): Result<Int> {
        return try {
            val currentTime = System.currentTimeMillis()
            val expiredQuery = collection
                .whereEqualTo("isActive", true)
                .whereLessThan("expiresAt", currentTime)
                .get()
                .await()
            
            val batch = firestore.batch()
            expiredQuery.documents.forEach { doc ->
                batch.update(doc.reference, "isActive", false)
            }
            
            batch.commit().await()
            Result.success(expiredQuery.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}