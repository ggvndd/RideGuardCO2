package com.capstoneco2.rideguard.service

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for sending push notifications to emergency contacts via FCM
 * Uses FCM REST API to send notifications to other devices
 */
@Singleton
class PushNotificationService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "PushNotificationService"
        private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
        // TODO: Replace with your actual FCM Server Key from Firebase Console
        // Go to Project Settings → Cloud Messaging → Server Key
        private const val FCM_SERVER_KEY = "YOUR_FCM_SERVER_KEY_HERE"
    }
    
    private val client = OkHttpClient()
    
    /**
     * Send emergency notification to all devices of a specific user (emergency contact)
     */
    suspend fun sendEmergencyNotificationToUser(
        context: Context,
        contactUserId: String,
        crashVictimName: String,
        latitude: Double,
        longitude: Double,
        crashId: String = "REAL_CRASH"
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Get all FCM tokens for this emergency contact user
            val fcmTokens = getFCMTokensForUser(contactUserId)
            
            if (fcmTokens.isEmpty()) {
                Log.w(TAG, "No FCM tokens found for user: $contactUserId")
                return@withContext Result.failure(Exception("No FCM tokens found for emergency contact"))
            }
            
            var successCount = 0
            val errors = mutableListOf<String>()
            
            // Send notification to each device/token
            fcmTokens.forEach { token ->
                try {
                    val success = sendFCMNotification(
                        token = token,
                        title = "🚨 EMERGENCY CONTACT ALERT",
                        body = "Your emergency contact $crashVictimName has been in a traffic accident. Location: $latitude, $longitude. Tap to help them get emergency assistance.",
                        crashVictimName = crashVictimName,
                        latitude = latitude,
                        longitude = longitude,
                        crashId = crashId
                    )
                    
                    if (success) {
                        successCount++
                        Log.d(TAG, "Successfully sent notification to token: ${token.take(20)}...")
                    } else {
                        errors.add("Failed to send to token: ${token.take(20)}...")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending to token ${token.take(20)}...: ${e.message}")
                    errors.add("Error: ${e.message}")
                }
            }
            
            if (successCount > 0) {
                Result.success(successCount)
            } else {
                Result.failure(Exception("Failed to send to any devices. Errors: ${errors.joinToString(", ")}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending emergency notifications: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Get all active FCM tokens for a specific user across all their devices
     */
    private suspend fun getFCMTokensForUser(userId: String): List<String> {
        return try {
            val querySnapshot = firestore.collection("fcm_tokens")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val tokens = querySnapshot.documents.mapNotNull { doc ->
                doc.getString("token")
            }.distinct()
            
            Log.d(TAG, "Found ${tokens.size} active FCM tokens for user: $userId")
            tokens
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching FCM tokens for user $userId: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Send FCM notification via REST API
     */
    private suspend fun sendFCMNotification(
        token: String,
        title: String,
        body: String,
        crashVictimName: String,
        latitude: Double,
        longitude: Double,
        crashId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if FCM server key is configured
            if (FCM_SERVER_KEY == "YOUR_FCM_SERVER_KEY_HERE") {
                Log.w(TAG, "FCM Server Key not configured. Cannot send real push notifications.")
                Log.i(TAG, "To enable real push notifications:")
                Log.i(TAG, "1. Go to Firebase Console → Project Settings → Cloud Messaging")
                Log.i(TAG, "2. Copy the 'Server Key' from the Cloud Messaging tab")
                Log.i(TAG, "3. Replace FCM_SERVER_KEY in PushNotificationService.kt")
                return@withContext false
            }
            
            // Create FCM payload
            val notification = JSONObject().apply {
                put("title", title)
                put("body", body)
                put("sound", "default")
                put("click_action", "FLUTTER_NOTIFICATION_CLICK")
            }
            
            val data = JSONObject().apply {
                put("emergency_type", "crash")
                put("user_role", "emergency_contact")
                put("crash_victim_name", crashVictimName)
                put("crash_id", crashId)
                put("latitude", latitude.toString())
                put("longitude", longitude.toString())
                put("navigate_to", "Blackbox")
            }
            
            val message = JSONObject().apply {
                put("to", token)
                put("notification", notification)
                put("data", data)
                put("priority", "high")
            }
            
            // Create HTTP request
            val requestBody = message.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(FCM_URL)
                .post(requestBody)
                .addHeader("Authorization", "key=$FCM_SERVER_KEY")
                .addHeader("Content-Type", "application/json")
                .build()
            
            // Send request
            val response = client.newCall(request).execute()
            val success = response.isSuccessful
            
            if (success) {
                Log.d(TAG, "FCM notification sent successfully to token: ${token.take(20)}...")
            } else {
                Log.e(TAG, "FCM notification failed. Response: ${response.code} - ${response.body?.string()}")
            }
            
            response.close()
            success
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending FCM notification: ${e.message}")
            false
        }
    }
    
    /**
     * Test method to check if FCM is properly configured
     */
    fun isFCMConfigured(): Boolean {
        return FCM_SERVER_KEY != "YOUR_FCM_SERVER_KEY_HERE" && FCM_SERVER_KEY.isNotEmpty()
    }
    
    /**
     * Get configuration status for debugging
     */
    fun getConfigurationStatus(): String {
        return if (isFCMConfigured()) {
            "✅ FCM Server Key configured - Real push notifications enabled"
        } else {
            "⚠️ FCM Server Key not configured - Using local notifications only"
        }
    }
}