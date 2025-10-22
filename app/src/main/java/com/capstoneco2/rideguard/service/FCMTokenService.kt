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
     * Save or update FCM token for a user account on a device
     * Each user account gets its own FCM token entry on the same device
     */
    suspend fun saveOrUpdateFCMToken(
        userId: String,
        userDisplayName: String,
        token: String,
        context: Context,
        appVersion: String = "1.0"
    ): Result<String> {
        return try {
            android.util.Log.d("FCMTokenService", "Saving FCM token for user: $userId on device")
            
            val deviceId = getDeviceId(context)
            val deviceName = getDeviceName(context)
            
            // Check if token already exists for this specific user on this device
            val existingTokenQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            if (!existingTokenQuery.isEmpty) {
                // Update existing token for this user
                val existingDoc = existingTokenQuery.documents.first()
                val updateData = mapOf(
                    "token" to token,
                    "lastUpdatedAt" to System.currentTimeMillis(),
                    "lastUsedAt" to System.currentTimeMillis(),
                    "appVersion" to appVersion,
                    "userDisplayName" to userDisplayName
                )
                
                existingDoc.reference.update(updateData).await()
                android.util.Log.d("FCMTokenService", "Updated existing FCM token for user: $userId on device: $deviceId")
                Result.success(existingDoc.id)
            } else {
                // Create new token record for this user account
                val isPrimary = checkIfPrimaryUser(deviceId)
                
                val fcmToken = FCMToken(
                    userId = userId,
                    token = token,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    userDisplayName = userDisplayName,
                    appVersion = appVersion,
                    isPrimary = isPrimary
                )
                
                val docRef = fcmTokensCollection.add(fcmToken.toMap()).await()
                android.util.Log.d("FCMTokenService", "Created new FCM token record for user: $userId on device: $deviceId (Primary: $isPrimary)")
                Result.success(docRef.id)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to save FCM token for user: $userId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all users' FCM tokens on a specific device
     */
    suspend fun getDeviceUserTokens(context: Context): Result<List<FCMToken>> {
        return try {
            val deviceId = getDeviceId(context)
            android.util.Log.d("FCMTokenService", "Retrieving all user FCM tokens for device: $deviceId")
            
            val tokensQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
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
                        userDisplayName = doc.getString("userDisplayName") ?: "",
                        platform = doc.getString("platform") ?: "android",
                        appVersion = doc.getString("appVersion") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        lastUpdatedAt = doc.getLong("lastUpdatedAt") ?: 0L,
                        lastUsedAt = doc.getLong("lastUsedAt") ?: 0L,
                        isActive = doc.getBoolean("isActive") ?: true,
                        isPrimary = doc.getBoolean("isPrimary") ?: false
                    )
                } catch (e: Exception) {
                    android.util.Log.w("FCMTokenService", "Failed to parse FCM token document: ${doc.id}", e)
                    null
                }
            }.sortedByDescending { it.lastUsedAt } // Most recently used first
            
            android.util.Log.d("FCMTokenService", "Found ${tokens.size} user accounts on device: $deviceId")
            Result.success(tokens)
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to retrieve device user tokens", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all devices for a specific user
     */
    suspend fun getUserDeviceTokens(userId: String): Result<List<FCMToken>> {
        return try {
            android.util.Log.d("FCMTokenService", "Retrieving FCM tokens for user: $userId across all devices")
            
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
                        userDisplayName = doc.getString("userDisplayName") ?: "",
                        platform = doc.getString("platform") ?: "android",
                        appVersion = doc.getString("appVersion") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        lastUpdatedAt = doc.getLong("lastUpdatedAt") ?: 0L,
                        lastUsedAt = doc.getLong("lastUsedAt") ?: 0L,
                        isActive = doc.getBoolean("isActive") ?: true,
                        isPrimary = doc.getBoolean("isPrimary") ?: false
                    )
                } catch (e: Exception) {
                    android.util.Log.w("FCMTokenService", "Failed to parse FCM token document: ${doc.id}", e)
                    null
                }
            }.sortedByDescending { it.lastUsedAt }
            
            android.util.Log.d("FCMTokenService", "Found ${tokens.size} devices for user: $userId")
            Result.success(tokens)
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to retrieve user device tokens", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update last used time for a user's token on current device
     */
    suspend fun updateLastUsed(userId: String, context: Context): Result<Unit> {
        return try {
            val deviceId = getDeviceId(context)
            
            val tokenQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            for (doc in tokenQuery.documents) {
                doc.reference.update("lastUsedAt", System.currentTimeMillis()).await()
            }
            
            android.util.Log.d("FCMTokenService", "Updated last used time for user: $userId on device: $deviceId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to update last used time", e)
            Result.failure(e)
        }
    }
    
    /**
     * Set a user as primary on this device (only one primary user per device)
     */
    suspend fun setPrimaryUser(userId: String, context: Context): Result<Unit> {
        return try {
            val deviceId = getDeviceId(context)
            
            // First, remove primary status from all users on this device
            val allUsersQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val batch = firestore.batch()
            
            // Set all users to non-primary
            for (doc in allUsersQuery.documents) {
                batch.update(doc.reference, "isPrimary", false)
            }
            
            // Set the specified user as primary
            val userTokenQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            for (doc in userTokenQuery.documents) {
                batch.update(doc.reference, "isPrimary", true)
                batch.update(doc.reference, "lastUsedAt", System.currentTimeMillis())
            }
            
            batch.commit().await()
            
            android.util.Log.d("FCMTokenService", "Set user: $userId as primary on device: $deviceId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to set primary user", e)
            Result.failure(e)
        }
    }
    
    /**
     * Deactivate FCM token for a specific user on current device (when user logs out)
     */
    suspend fun deactivateUserToken(userId: String, context: Context): Result<Unit> {
        return try {
            val deviceId = getDeviceId(context)
            
            val tokenQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            for (doc in tokenQuery.documents) {
                doc.reference.update(mapOf(
                    "isActive" to false,
                    "lastUpdatedAt" to System.currentTimeMillis()
                )).await()
            }
            
            android.util.Log.d("FCMTokenService", "Deactivated FCM token for user: $userId on device: $deviceId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenService", "Failed to deactivate user token", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if this is the first user on the device (becomes primary)
     */
    private suspend fun checkIfPrimaryUser(deviceId: String): Boolean {
        return try {
            val existingUsersQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            existingUsersQuery.isEmpty // If no existing users, this becomes primary
        } catch (e: Exception) {
            true // Default to primary if check fails
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
     * Get count of active users on a device
     */
    suspend fun getDeviceUserCount(context: Context): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            val activeTokensQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
            val snapshot = activeTokensQuery.get().await()
            
            val count = snapshot.size()
            Log.d("FCMTokenService", "Device $deviceId has $count active user accounts")
            
            Result.success(count)
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed to get device user count", e)
            Result.failure(e)
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