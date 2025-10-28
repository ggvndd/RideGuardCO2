package com.capstoneco2.rideguard.service

import android.util.Log
import com.capstoneco2.rideguard.config.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP service for sending SMS data to server via POST requests
 */
class SmsHttpService {
    
    companion object {
        private const val TAG = "SmsHttpService"
        
        // HTTP client configuration
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.HttpConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.HttpConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.HttpConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    /**
     * Send SMS data to server via HTTP POST request
     * This method matches the parameters expected by SmsService
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
                .url(ApiConfig.SmsEndpoints.RECEIVE_SMS_URL)
                .post(requestBody)
                .addHeader("Content-Type", ApiConfig.Headers.CONTENT_TYPE)
                .addHeader("User-Agent", ApiConfig.Headers.USER_AGENT)
                .apply {
                    // Add API key if configured
                    if (ApiConfig.API_KEY.isNotBlank() && ApiConfig.API_KEY != "your-api-key-here") {
                        addHeader(ApiConfig.Headers.API_KEY_HEADER, ApiConfig.API_KEY)
                    }
                }
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
     * This method matches the parameters expected by SmsService
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
                .url(ApiConfig.SmsEndpoints.RECEIVE_SMS_URL)
                .post(requestBody)
                .addHeader("Content-Type", ApiConfig.Headers.CONTENT_TYPE)
                .addHeader("User-Agent", ApiConfig.Headers.USER_AGENT)
                .apply {
                    // Add API key if configured
                    if (ApiConfig.API_KEY.isNotBlank() && ApiConfig.API_KEY != "your-api-key-here") {
                        addHeader(ApiConfig.Headers.API_KEY_HEADER, ApiConfig.API_KEY)
                    }
                }
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
            put("emergencyKeywordCount", emergencyKeywords.size)
            put("messageLength", message.length)
            put("platform", "android")
            put("messageType", if (isEmergency) "emergency" else "normal")
            
            // Optional fields
            deviceId?.let { put("deviceId", it) }
            userId?.let { put("userId", it) }
            
            // Additional metadata for server processing
            put("appVersion", "1.0")
            put("sdkVersion", android.os.Build.VERSION.SDK_INT)
            put("deviceModel", android.os.Build.MODEL)
            put("deviceManufacturer", android.os.Build.MANUFACTURER)
            
            // Processing metadata
            put("processingDelay", System.currentTimeMillis() - timestamp)
            put("messageHash", message.hashCode()) // For deduplication on server
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
                .url(ApiConfig.SmsEndpoints.SMS_PING_URL)
                .post(requestBody)
                .addHeader("Content-Type", ApiConfig.Headers.CONTENT_TYPE)
                .addHeader("User-Agent", ApiConfig.Headers.USER_AGENT)
                .apply {
                    // Add API key if configured
                    if (ApiConfig.API_KEY.isNotBlank() && ApiConfig.API_KEY != "your-api-key-here") {
                        addHeader(ApiConfig.Headers.API_KEY_HEADER, ApiConfig.API_KEY)
                    }
                }
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
     * Send emergency SMS with high priority
     */
    suspend fun sendEmergencySms(
        sender: String,
        message: String,
        timestamp: Long,
        emergencyKeywords: List<String>,
        location: String? = null,
        deviceId: String? = null,
        userId: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.w(TAG, "🚨 SENDING EMERGENCY SMS TO SERVER 🚨")
            
            val jsonPayload = createEmergencySmsJsonPayload(
                sender, message, timestamp, emergencyKeywords, 
                location, deviceId, userId
            )
            
            val requestBody = jsonPayload.toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(ApiConfig.SmsEndpoints.EMERGENCY_SMS_URL)
                .post(requestBody)
                .addHeader("Content-Type", ApiConfig.Headers.CONTENT_TYPE)
                .addHeader("User-Agent", ApiConfig.Headers.USER_AGENT)
                .addHeader("X-Priority", "urgent") // Mark as urgent
                .addHeader("X-Message-Type", "emergency")
                .apply {
                    // Add API key if configured
                    if (ApiConfig.API_KEY.isNotBlank() && ApiConfig.API_KEY != "your-api-key-here") {
                        addHeader(ApiConfig.Headers.API_KEY_HEADER, ApiConfig.API_KEY)
                    }
                }
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            response.use { resp ->
                val responseBody = resp.body?.string() ?: ""
                
                if (resp.isSuccessful) {
                    Log.i(TAG, "🚨 Emergency SMS successfully sent to server")
                    Result.success(responseBody)
                } else {
                    Log.e(TAG, "🚨 Emergency SMS send failed: ${resp.code}")
                    Result.failure(IOException("Emergency HTTP ${resp.code}: $responseBody"))
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "🚨 Critical error sending emergency SMS", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create JSON payload specifically for emergency SMS
     */
    private fun createEmergencySmsJsonPayload(
        sender: String,
        message: String,
        timestamp: Long,
        emergencyKeywords: List<String>,
        location: String?,
        deviceId: String?,
        userId: String?
    ): String {
        val jsonObject = JSONObject().apply {
            // Basic SMS data
            put("sender", sender)
            put("message", message)
            put("timestamp", timestamp)
            put("receivedAt", System.currentTimeMillis())
            
            // Emergency-specific data
            put("isEmergency", true)
            put("messageType", "emergency")
            put("priority", "urgent")
            put("emergencyKeywords", emergencyKeywords.joinToString(","))
            put("emergencyKeywordCount", emergencyKeywords.size)
            
            // Location if available
            location?.let { put("location", it) }
            
            // Device/User context
            deviceId?.let { put("deviceId", it) }
            userId?.let { put("userId", it) }
            
            // Metadata
            put("platform", "android")
            put("appVersion", "1.0")
            put("processingDelay", System.currentTimeMillis() - timestamp)
            put("alertLevel", "high")
        }
        
        return jsonObject.toString()
    }
    
    /**
     * Update server endpoint (useful for switching between dev/prod)
     */
    fun updateEndpoint(newEndpoint: String) {
        Log.i(TAG, "Endpoint updated from $SMS_ENDPOINT to $newEndpoint")
        // Note: This would require making SMS_ENDPOINT mutable
        // For now, this is just a placeholder for the concept
    }
    
    /**
     * Check if server is reachable (simple health check)
     */
    suspend fun isServerReachable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(ApiConfig.SmsEndpoints.SMS_STATUS_URL)
                .get()
                .addHeader("User-Agent", ApiConfig.Headers.USER_AGENT)
                .apply {
                    // Add API key if configured
                    if (ApiConfig.API_KEY.isNotBlank() && ApiConfig.API_KEY != "your-api-key-here") {
                        addHeader(ApiConfig.Headers.API_KEY_HEADER, ApiConfig.API_KEY)
                    }
                }
                .build()
            
            val response = httpClient.newCall(request).execute()
            response.use { resp ->
                val isReachable = resp.isSuccessful
                Log.d(TAG, "Server reachability check: ${if (isReachable) "✅ Online" else "❌ Offline (${resp.code})"}")
                isReachable
            }
        } catch (e: Exception) {
            Log.w(TAG, "Server reachability check failed", e)
            false
        }
    }
    
    /**
     * Get current endpoint URL (for debugging)
     */
    fun getEndpointUrl(): String = ApiConfig.SmsEndpoints.RECEIVE_SMS_URL
    
    /**
     * Get HTTP client configuration info
     */
    fun getClientInfo(): String {
        return "HTTP Client - Connect: 30s, Read: 30s, Write: 30s"
    }
}