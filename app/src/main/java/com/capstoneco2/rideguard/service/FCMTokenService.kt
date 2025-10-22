package com.capstoneco2.rideguard.service

import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.capstoneco2.rideguard.data.FCMToken
import com.capstoneco2.rideguard.data.toMap
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing FCM tokens in Firestore
 * Handles multiple user accounts per device (one FCM token entry per user per device)
 */
@Singleton
class FCMTokenService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val db: FirebaseFirestore = firestore
    private val fcmTokensCollection = db.collection("fcm_tokens")
    
    /**
     * Save or update FCM token for current user on this device
     * One FCM token document per device, userId gets updated when different users sign in
     */
    suspend fun saveOrUpdateFCMToken(
        userId: String,
        userDisplayName: String,
        token: String,
        context: Context,
        appVersion: String = "1.0"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            val deviceName = getDeviceName(context)
            val currentTime = System.currentTimeMillis()
            
            android.util.Log.d("FCMTokenService", "Saving/updating FCM token for user: $userId on device: $deviceId")
            
            // Look for existing FCM token document for this device (by deviceId and token)
            val existingTokenQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("token", token)
                .limit(1)
                .get()
                .await()
            
            val documentId: String
            
            if (existingTokenQuery.documents.isNotEmpty()) {
                // Update existing document with new user information
                val existingDoc = existingTokenQuery.documents.first()
                documentId = existingDoc.id
                
                val updateData = mapOf(
                    "userId" to userId,
                    "userDisplayName" to userDisplayName,
                    "lastUpdatedAt" to currentTime,
                    "lastUsedAt" to currentTime,
                    "appVersion" to appVersion,
                    "isActive" to true
                )
                
                existingDoc.reference.update(updateData).await()
                android.util.Log.d("FCMTokenService", "Updated existing FCM token document: $documentId with new user: $userId")
                
            } else {
                // Create new document for this device
                val fcmToken = FCMToken(
                    userId = userId,
                    token = token,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    userDisplayName = userDisplayName,
                    platform = "android",
                    appVersion = appVersion,
                    createdAt = currentTime,
                    lastUpdatedAt = currentTime,
                    lastUsedAt = currentTime,
                    isActive = true,
                    isPrimary = true // Single user per device, so always primary
                )
                
                val docRef = fcmTokensCollection.add(fcmToken.toMap()).await()
                documentId = docRef.id
                android.util.Log.d("FCMTokenService", "Created new FCM token document: $documentId for user: $userId")
            }
            
            Result.success(documentId)
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to save/update FCM token", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get the current FCM token for this device
     */
    suspend fun getDeviceToken(context: Context): Result<FCMToken?> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            android.util.Log.d("FCMTokenService", "Getting FCM token for device: $deviceId")
            
            val tokenQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()
            
            val token = tokenQuery.documents.firstOrNull()?.let { doc ->
                FCMToken(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    token = doc.getString("token") ?: "",
                    deviceId = doc.getString("deviceId") ?: "",
                    deviceName = doc.getString("deviceName") ?: "",
                    userDisplayName = doc.getString("userDisplayName") ?: "",
                    platform = doc.getString("platform") ?: "android",
                    appVersion = doc.getString("appVersion") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    lastUpdatedAt = doc.getLong("lastUpdatedAt") ?: 0L,
                    lastUsedAt = doc.getLong("lastUsedAt") ?: 0L,
                    isActive = doc.getBoolean("isActive") ?: true,
                    isPrimary = doc.getBoolean("isPrimary") ?: false
                )
            }
            
            if (token != null) {
                android.util.Log.d("FCMTokenService", "Found FCM token for device: $deviceId, current user: ${token.userId}")
            } else {
                android.util.Log.d("FCMTokenService", "No FCM token found for device: $deviceId")
            }
            
            Result.success(token)
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to get device token", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update the current user for this device's FCM token
     */
    suspend fun updateDeviceUser(userId: String, userDisplayName: String, context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            android.util.Log.d("FCMTokenService", "Updating device user to: $userId ($userDisplayName)")
            
            val tokenQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()
            
            if (tokenQuery.documents.isNotEmpty()) {
                val doc = tokenQuery.documents.first()
                val updateData = mapOf(
                    "userId" to userId,
                    "userDisplayName" to userDisplayName,
                    "lastUsedAt" to System.currentTimeMillis(),
                    "lastUpdatedAt" to System.currentTimeMillis()
                )
                
                doc.reference.update(updateData).await()
                android.util.Log.d("FCMTokenService", "Successfully updated device user to: $userId")
                Result.success(Unit)
            } else {
                android.util.Log.w("FCMTokenService", "No FCM token found for device: $deviceId")
                Result.failure(Exception("No FCM token found for device"))
            }
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to update device user", e)
            Result.failure(e)
        }
    }
    
    /**
     * Deactivate FCM token for this device (when user logs out and no one else uses the device)
     */
    suspend fun deactivateDeviceToken(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            
            val tokenQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()
            
            if (tokenQuery.documents.isNotEmpty()) {
                val doc = tokenQuery.documents.first()
                doc.reference.update(mapOf(
                    "isActive" to false,
                    "lastUpdatedAt" to System.currentTimeMillis()
                )).await()
                
                android.util.Log.d("FCMTokenService", "Deactivated FCM token for device: $deviceId")
                Result.success(Unit)
            } else {
                android.util.Log.w("FCMTokenService", "No active FCM token found for device: $deviceId")
                Result.success(Unit) // Not an error if already inactive
            }
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to deactivate device token", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update last used timestamp for current device token
     */
    suspend fun updateLastUsed(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            
            val tokenQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()
            
            if (tokenQuery.documents.isNotEmpty()) {
                val doc = tokenQuery.documents.first()
                doc.reference.update("lastUsedAt", System.currentTimeMillis()).await()
                android.util.Log.d("FCMTokenService", "Updated last used time for device: $deviceId")
            }
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to update last used time", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get count of active FCM tokens (should be 1 per device in this model)
     */
    suspend fun getActiveTokenCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val activeTokensQuery = fcmTokensCollection
                .whereEqualTo("isActive", true)
            val snapshot = activeTokensQuery.get().await()
            
            val count = snapshot.size()
            Log.d("FCMTokenService", "Total active FCM tokens: $count")
            
            Result.success(count)
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed to get active token count", e)
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
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
    }

    /**
     * Clean up inactive FCM tokens (older than 30 days)
     * Uses client-side filtering to avoid complex Firestore indexes
     */
    suspend fun cleanupInactiveFCMTokens(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d("FCMTokenService", "Starting cleanup of inactive FCM tokens")
            
            // First check if fcm_tokens collection exists and has any documents
            val collectionCheck = fcmTokensCollection.limit(1).get().await()
            if (collectionCheck.isEmpty) {
                Log.d("FCMTokenService", "No FCM tokens found in collection, skipping cleanup")
                return@withContext Result.success(0)
            }
            
            val thirtyDaysAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            
            // Get all active tokens (simple query, no composite index needed)
            val activeTokensQuery = fcmTokensCollection
                .whereEqualTo("isActive", true)
            
            val activeTokensSnapshot = activeTokensQuery.get().await()
            
            if (activeTokensSnapshot.isEmpty) {
                Log.d("FCMTokenService", "No active FCM tokens found, skipping cleanup")
                return@withContext Result.success(0)
            }
            
            var deletedCount = 0
            val batch = firestore.batch()
            
            // Filter old tokens on the client side to avoid composite index requirement
            for (document in activeTokensSnapshot.documents) {
                val lastUsedAt = document.getLong("lastUsedAt") ?: 0L
                
                // Check if token is older than 30 days
                if (lastUsedAt < thirtyDaysAgo) {
                    batch.update(document.reference, "isActive", false)
                    deletedCount++
                    Log.d("FCMTokenService", "Marking old FCM token as inactive: ${document.id} (last used: ${Date(lastUsedAt)})")
                }
            }
            
            if (deletedCount > 0) {
                batch.commit().await()
                Log.i("FCMTokenService", "Successfully deactivated $deletedCount old FCM tokens")
            } else {
                Log.d("FCMTokenService", "No old FCM tokens found for cleanup")
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed to cleanup inactive FCM tokens", e)
            Result.failure(e)
        }
    }
    
    /**
     * Alternative cleanup method using server-side filtering (requires Firestore composite index)
     * Use this method after creating the required composite index in Firebase Console
     * Index URL: https://console.firebase.google.com/v1/r/project/rideguard-9b11e/firestore/indexes
     * 
     * Required composite index fields (in order):
     * - isActive (Ascending)
     * - lastUsedAt (Ascending)
     * - __name__ (Ascending)
     */
    suspend fun cleanupInactiveFCMTokensServerSide(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d("FCMTokenService", "Starting server-side cleanup of inactive FCM tokens")
            
            val thirtyDaysAgo = Timestamp(Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L))
            
            // This query requires a composite index in Firestore
            val oldTokensQuery = fcmTokensCollection
                .whereEqualTo("isActive", true)
                .whereLessThan("lastUsedAt", thirtyDaysAgo.toDate().time)
            
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
                Log.i("FCMTokenService", "Successfully deactivated $deletedCount old FCM tokens (server-side)")
            } else {
                Log.d("FCMTokenService", "No old FCM tokens found for cleanup (server-side)")
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed server-side cleanup of inactive FCM tokens", e)
            Log.e("FCMTokenService", "If you see an index error, create the composite index in Firebase Console")
            Log.e("FCMTokenService", "Index URL provided in the error message above")
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
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
    }
    
    /**
     * Get primary user on device
     */
    suspend fun getPrimaryUser(context: Context): Result<FCMToken?> {
        return try {
            val deviceId = getDeviceId(context)
            
            val primaryUserQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isPrimary", true)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val primaryUser = primaryUserQuery.documents.firstOrNull()?.let { doc ->
                FCMToken(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    token = doc.getString("token") ?: "",
                    deviceId = doc.getString("deviceId") ?: "",
                    deviceName = doc.getString("deviceName") ?: "",
                    userDisplayName = doc.getString("userDisplayName") ?: "",
                    platform = doc.getString("platform") ?: "android",
                    appVersion = doc.getString("appVersion") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    lastUpdatedAt = doc.getLong("lastUpdatedAt") ?: 0L,
                    lastUsedAt = doc.getLong("lastUsedAt") ?: 0L,
                    isActive = doc.getBoolean("isActive") ?: true,
                    isPrimary = doc.getBoolean("isPrimary") ?: false
                )
            }
            
            Result.success(primaryUser)
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to get primary user", e)
            Result.failure(e)
        }
    }
}