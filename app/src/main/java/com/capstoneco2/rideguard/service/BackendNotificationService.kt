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
 * Service for sending push notifications to emergency contacts via backend API
 * Uses your existing Next.js backend FCM notification service
 */
@Singleton
class BackendNotificationService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    // Secondary constructor for manual instantiation
    constructor() : this(FirebaseFirestore.getInstance())
    companion object {
        private const val TAG = "BackendNotificationService"
        // TODO: Replace with your actual deployed backend URL
        private const val BACKEND_BASE_URL = "https://backend-rideguard.vercel.app" // e.g., "https://your-app.vercel.app"
        private const val NOTIFY_ENDPOINT = "/api/notify"
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
            
            // Send notification to each device/token via backend
            fcmTokens.forEach { token ->
                try {
                    val success = sendNotificationViaBackend(
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
                        Log.d(TAG, "Successfully sent notification via backend to token: ${token.take(20)}...")
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
            Log.e(TAG, "Error sending emergency notifications via backend: ${e.message}")
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
     * Send notification via your Next.js backend API
     */
    private suspend fun sendNotificationViaBackend(
        token: String,
        title: String,
        body: String,
        crashVictimName: String,
        latitude: Double,
        longitude: Double,
        crashId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if backend URL is configured
            if (BACKEND_BASE_URL == "YOUR_BACKEND_URL_HERE" || BACKEND_BASE_URL.contains("your-backend-domain")) {
                Log.w(TAG, "Backend URL not configured. Cannot send real push notifications.")
                Log.i(TAG, "Current BACKEND_BASE_URL: $BACKEND_BASE_URL")
                Log.i(TAG, "To enable real push notifications via backend:")
                Log.i(TAG, "1. Update BACKEND_BASE_URL with your deployed backend URL")
                Log.i(TAG, "2. Ensure your backend /api/notify endpoint is accessible")
                return@withContext false
            }
            
            // Create the notification payload that matches your backend API
            val requestBody = JSONObject().apply {
                put("token", token)
                put("title", title)
                put("body", body)
                // Add emergency-specific data as separate fields if needed
                put("emergency_type", "crash")
                put("user_role", "emergency_contact")
                put("crash_victim_name", crashVictimName)
                put("crash_id", crashId)
                put("latitude", latitude.toString())
                put("longitude", longitude.toString())
                put("navigate_to", "Blackbox")
            }
            
            // Create HTTP request to your backend
            val request = Request.Builder()
                .url("$BACKEND_BASE_URL$NOTIFY_ENDPOINT")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()
            
            // Send request to backend
            val response = client.newCall(request).execute()
            val success = response.isSuccessful
            
            if (success) {
                Log.d(TAG, "Backend notification sent successfully to token: ${token.take(20)}...")
                val responseBody = response.body?.string()
                Log.d(TAG, "Backend response: $responseBody")
            } else {
                Log.e(TAG, "Backend notification failed. Response: ${response.code} - ${response.body?.string()}")
            }
            
            response.close()
            success
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending notification via backend: ${e.message}")
            false
        }
    }
    
    /**
     * Test method to check if backend is properly configured and accessible
     */
    suspend fun testBackendConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isBackendConfigured()) {
                return@withContext Result.failure(Exception("Backend URL not configured: $BACKEND_BASE_URL"))
            }
            
            // Test connection to backend
            val request = Request.Builder()
                .url("$BACKEND_BASE_URL$NOTIFY_ENDPOINT")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            response.close()
            
            if (response.isSuccessful) {
                Result.success("✅ Backend connected successfully")
            } else {
                Result.failure(Exception("Backend responded with ${response.code}: $responseBody"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if backend is properly configured
     */
    fun isBackendConfigured(): Boolean {
        return BACKEND_BASE_URL != "YOUR_BACKEND_URL_HERE" && 
               !BACKEND_BASE_URL.contains("your-backend-domain") && 
               BACKEND_BASE_URL.isNotEmpty() &&
               BACKEND_BASE_URL.startsWith("http")
    }
    
    /**
     * Get configuration status for debugging
     */
    fun getConfigurationStatus(): String {
        return if (isBackendConfigured()) {
            "✅ Backend URL configured - Using Next.js FCM service"
        } else {
            "⚠️ Backend URL not configured - Update BackendNotificationService.kt"
        }
    }
}