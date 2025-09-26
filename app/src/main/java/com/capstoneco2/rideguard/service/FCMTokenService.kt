package com.capstoneco2.rideguard.service

import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import com.capstoneco2.rideguard.data.FCMToken
import com.capstoneco2.rideguard.data.toMap
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.util.Date

/**
 * Service for managing FCM tokens in Firestore
 * Handles multiple tokens per user (multiple devices)
 */
class FCMTokenService {
    
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val db: FirebaseFirestore = firestore
    private val fcmTokensCollection = db.collection("fcm_tokens")
    
    /**
     * Save or update FCM token for a user's device
     * If token already exists for this device, update it
     * If it's a new device, create a new record
     */
    suspend fun saveOrUpdateFCMToken(
        userId: String,
        token: String,
        context: Context,
        appVersion: String = "1.0"
    ): Result<String> {
        return try {
            android.util.Log.d("FCMTokenService", "Saving FCM token for user: $userId")
            
            val deviceId = getDeviceId(context)
            val deviceName = getDeviceName(context)
            
            // Check if token already exists for this device
            val existingTokenQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            if (!existingTokenQuery.isEmpty) {
                // Update existing token
                val existingDoc = existingTokenQuery.documents.first()
                val updateData = mapOf(
                    "token" to token,
                    "lastUpdatedAt" to System.currentTimeMillis(),
                    "appVersion" to appVersion
                )
                
                existingDoc.reference.update(updateData).await()
                android.util.Log.d("FCMTokenService", "Updated existing FCM token for device: $deviceId")
                Result.success(existingDoc.id)
            } else {
                // Create new token record
                val fcmToken = FCMToken(
                    userId = userId,
                    token = token,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appVersion = appVersion
                )
                
                val docRef = fcmTokensCollection.add(fcmToken.toMap()).await()
                android.util.Log.d("FCMTokenService", "Created new FCM token record for device: $deviceId")
                Result.success(docRef.id)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to save FCM token", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all active FCM tokens for a user (all their devices)
     */
    suspend fun getUserFCMTokens(userId: String): Result<List<FCMToken>> {
        return try {
            android.util.Log.d("FCMTokenService", "Retrieving FCM tokens for user: $userId")
            
            val tokensQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val tokens = tokensQuery.documents.mapNotNull { doc ->
                try {
                    FCMToken(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        token = doc.getString("token") ?: "",
                        deviceId = doc.getString("deviceId") ?: "",
                        deviceName = doc.getString("deviceName") ?: "",
                        platform = doc.getString("platform") ?: "android",
                        appVersion = doc.getString("appVersion") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        lastUpdatedAt = doc.getLong("lastUpdatedAt") ?: 0L,
                        isActive = doc.getBoolean("isActive") ?: true
                    )
                } catch (e: Exception) {
                    android.util.Log.w("FCMTokenService", "Failed to parse FCM token document: ${doc.id}", e)
                    null
                }
            }
            
            android.util.Log.d("FCMTokenService", "Found ${tokens.size} FCM tokens for user")
            Result.success(tokens)
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to retrieve FCM tokens", e)
            Result.failure(e)
        }
    }
    
    /**
     * Deactivate FCM token (when user logs out or uninstalls app)
     */
    suspend fun deactivateFCMToken(userId: String, context: Context): Result<Unit> {
        return try {
            val deviceId = getDeviceId(context)
            
            val tokenQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            for (doc in tokenQuery.documents) {
                doc.reference.update("isActive", false).await()
            }
            
            android.util.Log.d("FCMTokenService", "Deactivated FCM tokens for device: $deviceId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to deactivate FCM token", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get unique device identifier
     */
    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    
    /**
     * Get human-readable device name
     */
    private fun getDeviceName(context: Context): String {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.capitalize()
        } else {
            "${manufacturer.capitalize()} $model"
        }
    }

    /**
     * Clean up inactive FCM tokens (older than 30 days)
     */
    suspend fun cleanupInactiveFCMTokens(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d("FCMTokenService", "Starting cleanup of inactive FCM tokens for user: $userId")
            
            val thirtyDaysAgo = Timestamp(Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L))
            
            val oldTokensQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereLessThan("lastUpdatedAt", thirtyDaysAgo.toDate().time)
                .whereEqualTo("isActive", true)
            
            val oldTokensSnapshot = oldTokensQuery.get().await()
            
            var deletedCount = 0
            val batch = firestore.batch()
            
            for (document in oldTokensSnapshot.documents) {
                batch.update(document.reference, "isActive", false)
                deletedCount++
                Log.d("FCMTokenService", "Marking old FCM token as inactive: ${document.id}")
            }
            
            if (deletedCount > 0) {
                batch.commit().await()
                Log.i("FCMTokenService", "Successfully deactivated $deletedCount old FCM tokens for user: $userId")
            } else {
                Log.d("FCMTokenService", "No old FCM tokens found for cleanup for user: $userId")
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed to cleanup inactive FCM tokens", e)
            Result.failure(e)
        }
    }

    /**
     * Get count of active FCM tokens for a user
     */
    suspend fun getActiveFCMTokenCount(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val activeTokensQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
            val snapshot = activeTokensQuery.get().await()
            
            val count = snapshot.size()
            Log.d("FCMTokenService", "User $userId has $count active FCM tokens")
            
            Result.success(count)
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed to get active FCM token count", e)
            Result.failure(e)
        }
    }
}