package com.capstoneco2.rideguard.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP service for sending SMS data to server via POST requests
 */
class SmsHttpService {
    
    companion object {
        private const val TAG = "SmsHttpService"
        
        // TODO: Replace this with your actual server endpoint
        private const val SMS_ENDPOINT = "https://your-server-endpoint.com/api/sms"
        
        // HTTP client configuration
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Send SMS data to server via HTTP POST request
     */
    suspend fun sendSmsToServer(
        sender: String,
        message: String,
        timestamp: Long,
        isEmergency: Boolean = false,
        emergencyKeywords: List<String> = emptyList(),
        deviceId: String? = null,
        userId: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Preparing to send SMS data to server...")
            
            // Create JSON payload
            val jsonPayload = createSmsJsonPayload(
                sender = sender,
                message = message,
                timestamp = timestamp,
                isEmergency = isEmergency,
                emergencyKeywords = emergencyKeywords,
                deviceId = deviceId,
                userId = userId
            )
            
            Log.d(TAG, "JSON Payload: $jsonPayload")
            
            // Create HTTP request
            val requestBody = jsonPayload.toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(SMS_ENDPOINT)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "RideGuard-Android/1.0")
                .build()
            
            // Execute request
            val response = httpClient.newCall(request).execute()
            
            response.use { resp ->
                val responseBody = resp.body?.string() ?: ""
                
                if (resp.isSuccessful) {
                    Log.i(TAG, "✅ SMS successfully sent to server")
                    Log.i(TAG, "Server Response Code: ${resp.code}")
                    Log.i(TAG, "Server Response: $responseBody")
                    Result.success(responseBody)
                } else {
                    Log.e(TAG, "❌ Server returned error: ${resp.code}")
                    Log.e(TAG, "Error Response: $responseBody")
                    Result.failure(IOException("HTTP ${resp.code}: $responseBody"))
                }
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "❌ Network error sending SMS to server", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error sending SMS to server", e)
            Result.failure(e)
        }
    }
    
    /**
     * Send SMS data asynchronously (fire and forget)
     */
    fun sendSmsToServerAsync(
        sender: String,
        message: String,
        timestamp: Long,
        isEmergency: Boolean = false,
        emergencyKeywords: List<String> = emptyList(),
        deviceId: String? = null,
        userId: String? = null
    ) {
        try {
            Log.d(TAG, "Sending SMS data asynchronously...")
            
            val jsonPayload = createSmsJsonPayload(
                sender = sender,
                message = message,
                timestamp = timestamp,
                isEmergency = isEmergency,
                emergencyKeywords = emergencyKeywords,
                deviceId = deviceId,
                userId = userId
            )
            
            val requestBody = jsonPayload.toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(SMS_ENDPOINT)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "RideGuard-Android/1.0")
                .build()
            
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "❌ Async SMS send failed", e)
                }
                
                override fun onResponse(call: Call, response: Response) {
                    response.use { resp ->
                        val responseBody = resp.body?.string() ?: ""
                        
                        if (resp.isSuccessful) {
                            Log.i(TAG, "✅ Async SMS sent successfully (${resp.code})")
                            Log.d(TAG, "Response: $responseBody")
                        } else {
                            Log.e(TAG, "❌ Async SMS send error: ${resp.code}")
                            Log.e(TAG, "Error Response: $responseBody")
                        }
                    }
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error preparing async SMS request", e)
        }
    }
    
    /**
     * Create JSON payload for SMS data
     */
    private fun createSmsJsonPayload(
        sender: String,
        message: String,
        timestamp: Long,
        isEmergency: Boolean,
        emergencyKeywords: List<String>,
        deviceId: String?,
        userId: String?
    ): String {
        val jsonObject = JSONObject().apply {
            put("sender", sender)
            put("message", message)
            put("timestamp", timestamp)
            put("receivedAt", System.currentTimeMillis())
            put("isEmergency", isEmergency)
            put("emergencyKeywords", emergencyKeywords.joinToString(","))
            put("messageLength", message.length)
            put("platform", "android")
            
            // Optional fields
            deviceId?.let { put("deviceId", it) }
            userId?.let { put("userId", it) }
            
            // Additional metadata
            put("appVersion", "1.0")
            put("sdkVersion", android.os.Build.VERSION.SDK_INT)
        }
        
        return jsonObject.toString()
    }
    
    /**
     * Test server connectivity
     */
    suspend fun testServerConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Testing server connectivity...")
            
            // Send a simple ping request
            val testPayload = JSONObject().apply {
                put("type", "ping")
                put("timestamp", System.currentTimeMillis())
                put("source", "RideGuard-Android")
            }.toString()
            
            val requestBody = testPayload.toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$SMS_ENDPOINT/ping") // Assuming ping endpoint exists
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            response.use { resp ->
                if (resp.isSuccessful) {
                    Log.i(TAG, "✅ Server connectivity test successful")
                    Result.success(true)
                } else {
                    Log.w(TAG, "⚠️ Server connectivity test failed: ${resp.code}")
                    Result.success(false)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Server connectivity test failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get current endpoint URL (for debugging)
     */
    fun getEndpointUrl(): String = SMS_ENDPOINT
}