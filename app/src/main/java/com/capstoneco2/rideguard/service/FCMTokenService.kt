package com.capstoneco2.rideguard.service

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.capstoneco2.rideguard.data.FCMToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
     * Save or update FCM token for a user account on this device (multi-user per device)
     */
    suspend fun saveOrUpdateFCMToken(
        userId: String,
        userDisplayName: String,
        token: String,
        context: Context
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            val deviceName = getDeviceName(context)
            val appVersion = getAppVersion(context)
            val currentTime = System.currentTimeMillis()
            
            Log.d("FCMTokenService", "Saving/updating FCM token for multi-user device")
            Log.d("FCMTokenService", "User: $userId, Device: $deviceId, Token: ${token.take(20)}...")
            
            // Look for existing token document for this specific user-device-token combination
            val existingTokenQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("token", token)
                .limit(1)
            
            val existingTokenSnapshot = existingTokenQuery.get().await()
            
            if (!existingTokenSnapshot.isEmpty) {
                // Update existing document
                val existingDoc = existingTokenSnapshot.documents.first()
                val updates = mapOf(
                    "userDisplayName" to userDisplayName,
                    "lastUpdatedAt" to currentTime,
                    "lastUsedAt" to currentTime,
                    "isActive" to true,
                    "appVersion" to appVersion
                )
                
                existingDoc.reference.update(updates).await()
                Log.i("FCMTokenService", "Updated existing FCM token for user $userId: ${existingDoc.id}")
                return@withContext Result.success(existingDoc.id)
            } else {
                // Check if this is the first user on this device to set as primary
                val deviceUserCount = getDeviceUserCount(context).getOrNull() ?: 0
                val isPrimary = deviceUserCount == 0
                
                // Create new token document for this user
                val tokenData = mapOf(
                    "userId" to userId,
                    "userDisplayName" to userDisplayName,
                    "token" to token,
                    "deviceId" to deviceId,
                    "deviceName" to deviceName,
                    "platform" to "android",
                    "appVersion" to appVersion,
                    "createdAt" to currentTime,
                    "lastUpdatedAt" to currentTime,
                    "lastUsedAt" to currentTime,
                    "isActive" to true,
                    "isPrimary" to isPrimary
                )
                
                val newTokenRef = fcmTokensCollection.add(tokenData).await()
                Log.i("FCMTokenService", "Created new FCM token for user $userId: ${newTokenRef.id} (primary: $isPrimary)")
                return@withContext Result.success(newTokenRef.id)
            }
            
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed to save/update FCM token", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all users' FCM tokens on a specific device
     */
    suspend fun getDeviceUserTokens(context: Context): Result<List<FCMToken>> {
        return try {
            val deviceId = getDeviceId(context)
            Log.d("FCMTokenService", "Retrieving all user FCM tokens for device: $deviceId")
            
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
                    Log.w("FCMTokenService", "Failed to parse FCM token document: ${doc.id}", e)
                    null
                }
            }.sortedByDescending { it.lastUsedAt } // Most recently used first
            
            Log.d("FCMTokenService", "Found ${tokens.size} user accounts on device: $deviceId")
            Result.success(tokens)
            
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed to retrieve device user tokens", e)
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
            
            Log.d("FCMTokenService", "Updated last used time for user: $userId on device: $deviceId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed to update last used time", e)
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
    private fun getDeviceName(_context: Context): String {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
    }

    /**
     * Get app version name
     */
    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Smart cleanup: Only clean up current user's old tokens to avoid permission issues
     */
    suspend fun cleanupInactiveFCMTokens(context: Context): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Only cleanup current user's tokens to avoid permission issues
            val currentUserId = getCurrentUserId(context)
            if (currentUserId == null) {
                Log.d("FCMTokenService", "No current user, skipping cleanup")
                return@withContext Result.success(0)
            }
            
            Log.d("FCMTokenService", "Starting smart cleanup for current user: $currentUserId")
            
            val thirtyDaysAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            val deviceId = getDeviceId(context)
            
            Log.d("FCMTokenService", "Looking for tokens older than 30 days for user $currentUserId on device $deviceId")
            
            // Only look at current user's tokens on this device
            val userTokensQuery = fcmTokensCollection
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
            
            val userTokensSnapshot = userTokensQuery.get().await()
            
            var cleanedCount = 0
            for (doc in userTokensSnapshot.documents) {
                val lastUsedAt = doc.getLong("lastUsedAt") ?: 0L
                
                if (lastUsedAt < thirtyDaysAgo) {
                    doc.reference.update("isActive", false).await()
                    cleanedCount++
                    Log.d("FCMTokenService", "Deactivated old token for current user: ${doc.id}")
                }
            }
            
            Log.i("FCMTokenService", "Smart cleanup completed: $cleanedCount tokens deactivated")
            Result.success(cleanedCount)
            
        } catch (_: Exception) {
            Log.e("FCMTokenService", "Failed smart cleanup")
            Result.failure(Exception("Failed smart cleanup"))
        }
    }
    
    private fun getCurrentUserId(_context: Context): String? {
        return try {
            // Get current user from Firebase Auth (same as MainActivity)
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid
            
            Log.d("FCMTokenService", "Retrieved current user ID from Firebase Auth: $userId")
            userId
        } catch (_: Exception) {
            Log.w("FCMTokenService", "Failed to get current user ID from Firebase Auth")
            null
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
            Log.e("FCMTokenService", "Failed to get primary user", e)
            Result.failure(e)
        }
    }

    /**
     * Set a user as the primary user on this device
     */
    suspend fun setPrimaryUser(userId: String, context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            Log.d("FCMTokenService", "Setting primary user: $userId on device: $deviceId")
            
            val batch = firestore.batch()
            
            // First, remove primary status from all users on this device
            val allDeviceTokensQuery = fcmTokensCollection
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
            
            val allTokensSnapshot = allDeviceTokensQuery.get().await()
            
            for (doc in allTokensSnapshot.documents) {
                batch.update(doc.reference, "isPrimary", false)
            }
            
            // Then set the specified user as primary
            val userTokenQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
                .limit(1)
            
            val userTokenSnapshot = userTokenQuery.get().await()
            
            if (!userTokenSnapshot.isEmpty) {
                val userDoc = userTokenSnapshot.documents.first()
                batch.update(userDoc.reference, "isPrimary", true)
                batch.update(userDoc.reference, "lastUsedAt", System.currentTimeMillis())
            }
            
            batch.commit().await()
            Log.i("FCMTokenService", "Successfully set $userId as primary user on device $deviceId")
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed to set primary user", e)
            Result.failure(e)
        }
    }

    /**
     * Deactivate a user's FCM token on this device
     */
    suspend fun deactivateUserToken(userId: String, context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            Log.d("FCMTokenService", "Deactivating FCM token for user: $userId on device: $deviceId")
            
            val userTokenQuery = fcmTokensCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("isActive", true)
            
            val userTokenSnapshot = userTokenQuery.get().await()
            
            val batch = firestore.batch()
            var deactivatedCount = 0
            
            for (doc in userTokenSnapshot.documents) {
                batch.update(doc.reference, "isActive", false)
                batch.update(doc.reference, "lastUpdatedAt", System.currentTimeMillis())
                deactivatedCount++
            }
            
            if (deactivatedCount > 0) {
                batch.commit().await()
                Log.i("FCMTokenService", "Deactivated $deactivatedCount FCM tokens for user $userId")
                
                // If this was the primary user, set another user as primary
                val wasPrimary = userTokenSnapshot.documents.any { 
                    it.getBoolean("isPrimary") ?: false 
                }
                
                if (wasPrimary) {
                    // Find another active user to make primary
                    val remainingUsersQuery = fcmTokensCollection
                        .whereEqualTo("deviceId", deviceId)
                        .whereEqualTo("isActive", true)
                        .limit(1)
                    
                    val remainingUsersSnapshot = remainingUsersQuery.get().await()
                    
                    if (!remainingUsersSnapshot.isEmpty) {
                        val newPrimaryDoc = remainingUsersSnapshot.documents.first()
                        newPrimaryDoc.reference.update("isPrimary", true).await()
                        Log.i("FCMTokenService", "Set new primary user: ${newPrimaryDoc.getString("userId")}")
                    }
                }
            } else {
                Log.w("FCMTokenService", "No active tokens found for user $userId on device $deviceId")
            }
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e("FCMTokenService", "Failed to deactivate user token", e)
            Result.failure(e)
        }
    }
}