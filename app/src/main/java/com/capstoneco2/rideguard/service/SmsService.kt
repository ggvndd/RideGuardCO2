package com.capstoneco2.rideguard.service

import android.content.Context
import android.util.Log
import com.capstoneco2.rideguard.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Service to handle SMS message processing and analysis
 * Provides logging and potential emergency detection logic
 */
class SmsService {
    
    private val httpService = SmsHttpService()
    
    // Gateway settings
    private var isGatewayEnabled = false
    
    companion object {
        private const val TAG = "SmsService"
        private const val PREFS_NAME = "sms_gateway_prefs"
        private const val KEY_GATEWAY_ENABLED = "gateway_enabled"
        
        // Keywords that might indicate emergency situations
        private val EMERGENCY_KEYWORDS = listOf(
            "help", "emergency", "urgent", "accident", "danger", "fire", "police", 
            "ambulance", "hospital", "injured", "hurt", "trapped", "stuck", "lost",
            "kidnapped", "robbery", "theft", "attack", "violence", "threat",
            "socorro", "emergencia", "urgente", "accidente", "peligro", "fuego",
            "policia", "ambulancia", "hospital", "herido", "atrapado", "perdido",
            "crash_id", "rideguard_id", "longitude", "latitude" // RideGuard crash data indicators
        )
    }
    
    /**
     * Process incoming SMS message for logging and potential emergency detection
     */
    fun processSmsMessage(
        context: Context,
        sender: String,
        message: String,
        timestamp: Long
    ) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(timestamp))
            

            
            // Check for crash data first
            val crashData = parseCrashData(message)
            val isCrashData = crashData != null
            
            // Check for emergency keywords
            val emergencyKeywordsFound = checkForEmergencyKeywords(message)
            val isEmergency = emergencyKeywordsFound.isNotEmpty() || isCrashData
            
            if (isCrashData) {
                handlePotentialEmergency(context, sender, message, listOf("crash_data"), crashData)
            } else if (isEmergency) {
                handlePotentialEmergency(context, sender, message, emergencyKeywordsFound, null)
            }
            
            // Send SMS data to server via HTTP POST (only if gateway is enabled)
            if (isGatewayEnabled(context)) {
                sendSmsDataToServer(context, sender, message, timestamp, isEmergency, emergencyKeywordsFound, crashData)
            }
            

            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing SMS message", e)
        }
    }
    
    /**
     * Send SMS data to server via HTTP service
     * This method acts as a bridge between SmsService and SmsHttpService
     */
    private fun sendSmsDataToServer(
        context: Context,
        sender: String,
        message: String,
        timestamp: Long,
        isEmergency: Boolean,
        emergencyKeywords: List<String>,
        crashData: CrashData? = null
    ) {
        try {

            val deviceId = getDeviceId(context)
            val userId = getUserId(context)
            
            // Use coroutine scope to send data asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = if (isEmergency) {
                        val location = crashData?.let { "${it.latitude},${it.longitude}" }
                        
                        httpService.sendEmergencySms(
                            sender = sender,
                            message = message,
                            timestamp = timestamp,
                            emergencyKeywords = emergencyKeywords,
                            location = location,
                            deviceId = deviceId,
                            userId = userId,
                            crashData = crashData
                        )
                    } else {
                        httpService.sendSmsToServer(
                            sender = sender,
                            message = message,
                            timestamp = timestamp,
                            isEmergency = isEmergency,
                            emergencyKeywords = emergencyKeywords,
                            deviceId = deviceId,
                            userId = userId,
                            crashData = crashData
                        )
                    }
                    
                    result.fold(
                        onSuccess = { response ->
                            // SMS data successfully sent to server
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Failed to send SMS data to server", error)
                        }
                    )
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception while sending SMS data to server", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error preparing SMS data for server transmission", e)
        }
    }
    
    /**
     * Parse structured crash data from SMS message using regex patterns
     * Supports multiple formats:
     * - Line by line: "crash_id: A\nrideguard_id: A\nlongitude: -7.7676\nlatitude: 110.3698"
     * - Comma separated: "crash_id:A, rideguard_id: A\nlongitude: -7.7676, latitude: 110.3698"
     * - Mixed formats with various spacing
     */
    private fun parseCrashData(message: String): CrashData? {
        try {
            
            val crashData = CrashData()
            
            // Define regex patterns for each field with flexible formatting
            val patterns = mapOf(
                "crash_id" to listOf(
                    "crash_id\\s*:\\s*([^,\\n\\r]+)".toRegex(RegexOption.IGNORE_CASE),
                    "crashid\\s*:\\s*([^,\\n\\r]+)".toRegex(RegexOption.IGNORE_CASE),
                    "crash\\s*id\\s*:\\s*([^,\\n\\r]+)".toRegex(RegexOption.IGNORE_CASE)
                ),
                "rideguard_id" to listOf(
                    "rideguard_id\\s*:\\s*([^,\\n\\r]+)".toRegex(RegexOption.IGNORE_CASE),
                    "rideguardid\\s*:\\s*([^,\\n\\r]+)".toRegex(RegexOption.IGNORE_CASE),
                    "rideguard\\s*id\\s*:\\s*([^,\\n\\r]+)".toRegex(RegexOption.IGNORE_CASE)
                ),
                "longitude" to listOf(
                    "longitude\\s*:\\s*([\\-+]?\\d*\\.?\\d+)".toRegex(RegexOption.IGNORE_CASE),
                    "long\\s*:\\s*([\\-+]?\\d*\\.?\\d+)".toRegex(RegexOption.IGNORE_CASE),
                    "lng\\s*:\\s*([\\-+]?\\d*\\.?\\d+)".toRegex(RegexOption.IGNORE_CASE)
                ),
                "latitude" to listOf(
                    "latitude\\s*:\\s*([\\-+]?\\d*\\.?\\d+)".toRegex(RegexOption.IGNORE_CASE),
                    "lat\\s*:\\s*([\\-+]?\\d*\\.?\\d+)".toRegex(RegexOption.IGNORE_CASE)
                )
            )
            
            // Try to extract crash_id
            patterns["crash_id"]?.forEach { pattern ->
                if (crashData.crashId == null) {
                    val match = pattern.find(message)
                    if (match != null) {
                        crashData.crashId = match.groupValues[1].trim()
                    }
                }
            }
            
            // Try to extract rideguard_id
            patterns["rideguard_id"]?.forEach { pattern ->
                if (crashData.rideguardId == null) {
                    val match = pattern.find(message)
                    if (match != null) {
                        crashData.rideguardId = match.groupValues[1].trim()
                    }
                }
            }
            
            // Try to extract longitude
            patterns["longitude"]?.forEach { pattern ->
                if (crashData.longitude == null) {
                    val match = pattern.find(message)
                    if (match != null) {
                        val longitudeStr = match.groupValues[1].trim()
                        crashData.longitude = longitudeStr.toDoubleOrNull()
                    }
                }
            }
            
            // Try to extract latitude
            patterns["latitude"]?.forEach { pattern ->
                if (crashData.latitude == null) {
                    val match = pattern.find(message)
                    if (match != null) {
                        val latitudeStr = match.groupValues[1].trim()
                        crashData.latitude = latitudeStr.toDoubleOrNull()
                    }
                }
            }
            
            // Check if we have valid crash data
            val isValidCrashData = crashData.crashId != null && 
                                  crashData.rideguardId != null && 
                                  crashData.longitude != null && 
                                  crashData.latitude != null
            
            if (isValidCrashData) {
                return crashData
            } else {
                return parseCrashDataFallback(message)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing crash data", e)
            return null
        }
    }
    
    /**
     * Fallback parser using simple keyword extraction
     * For cases where regex patterns might miss unusual formatting
     */
    private fun parseCrashDataFallback(message: String): CrashData? {
        try {
            
            val crashData = CrashData()
            val cleanMessage = message.replace("\\s+".toRegex(), " ").lowercase()
            
            // Simple keyword-based extraction
            val keywordMap = mapOf(
                "crash_id" to listOf("crash_id", "crashid", "crash id"),
                "rideguard_id" to listOf("rideguard_id", "rideguardid", "rideguard id"),
                "longitude" to listOf("longitude", "long", "lng"),
                "latitude" to listOf("latitude", "lat")
            )
            
            for ((field, keywords) in keywordMap) {
                for (keyword in keywords) {
                    val keywordPattern = "$keyword\\s*:\\s*([^\\s,]+)".toRegex()
                    val match = keywordPattern.find(cleanMessage)
                    if (match != null) {
                        val value = match.groupValues[1].trim()
                        
                        when (field) {
                            "crash_id" -> {
                                if (crashData.crashId == null) {
                                    crashData.crashId = value.uppercase()
                                }
                            }
                            "rideguard_id" -> {
                                if (crashData.rideguardId == null) {
                                    crashData.rideguardId = value.uppercase()
                                }
                            }
                            "longitude" -> {
                                if (crashData.longitude == null) {
                                    crashData.longitude = value.toDoubleOrNull()
                                }
                            }
                            "latitude" -> {
                                if (crashData.latitude == null) {
                                    crashData.latitude = value.toDoubleOrNull()
                                }
                            }
                        }
                        break // Found with this keyword, move to next field
                    }
                }
            }
            
            // Try to extract numeric values as coordinates if field names are missing
            if (crashData.longitude == null || crashData.latitude == null) {
                val numberPattern = "([\\-+]?\\d+\\.\\d+)".toRegex()
                val numbers = numberPattern.findAll(message).map { it.value.toDoubleOrNull() }.filterNotNull().toList()
                
                if (numbers.size >= 2) {
                    // Assume first number is longitude, second is latitude
                    if (crashData.longitude == null) {
                        crashData.longitude = numbers[0]
                    }
                    if (crashData.latitude == null) {
                        crashData.latitude = numbers[1]
                    }
                }
            }
            
            // Try to extract single letter/number IDs if field names are missing
            if (crashData.crashId == null || crashData.rideguardId == null) {
                val letterPattern = "([A-Z])".toRegex()
                val letters = letterPattern.findAll(message.uppercase()).map { it.value }.toList()
                
                if (letters.isNotEmpty()) {
                    if (crashData.crashId == null) {
                        crashData.crashId = letters[0]
                    }
                    if (crashData.rideguardId == null && letters.size > 1) {
                        crashData.rideguardId = letters[1]
                    } else if (crashData.rideguardId == null) {
                        crashData.rideguardId = letters[0] // Use same if only one letter
                    }
                }
            }
            
            // Final validation
            val isValidCrashData = crashData.crashId != null && 
                                  crashData.rideguardId != null && 
                                  crashData.longitude != null && 
                                  crashData.latitude != null
            
            if (isValidCrashData) {
                return crashData
            } else {
                return null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in fallback parser", e)
            return null
        }
    }
    
    /**
     * Data class to hold parsed crash information
     */
    data class CrashData(
        var crashId: String? = null,
        var rideguardId: String? = null,
        var longitude: Double? = null,
        var latitude: Double? = null
    ) {
        fun toJsonString(): String {
            return """
                {
                    "crash_id": "$crashId",
                    "rideguard_id": "$rideguardId", 
                    "longitude": $longitude,
                    "latitude": $latitude,
                    "timestamp": ${System.currentTimeMillis()},
                    "parsed_at": "${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
                }
            """.trimIndent()
        }
    }

    /**
     * Get device ID for tracking (simplified version)
     */
    private fun getDeviceId(context: Context): String? {
        return try {
            // Using a simple approach - you might want to use Firebase Installation ID or similar
            android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not get device ID", e)
            null
        }
    }
    
    /**
     * Get user ID if available (placeholder for your authentication system)
     */
    private fun getUserId(context: Context): String? {
        return try {
            // User ID integration with authentication system can be added here
            null
        } catch (e: Exception) {
            Log.w(TAG, "Could not get user ID", e)
            null
        }
    }
    
    /**
     * Check if SMS contains emergency keywords
     */
    private fun checkForEmergencyKeywords(message: String): List<String> {
        val foundKeywords = mutableListOf<String>()
        val lowerCaseMessage = message.lowercase()
        
        for (keyword in EMERGENCY_KEYWORDS) {
            if (lowerCaseMessage.contains(keyword.lowercase())) {
                foundKeywords.add(keyword)
            }
        }
        
        return foundKeywords
    }
    
    /**
     * Handle potential emergency SMS
     */
    private fun handlePotentialEmergency(
        context: Context,
        sender: String,
        message: String,
        keywords: List<String>,
        crashData: CrashData? = null
    ) {
        try {
            
            // Show a high-priority notification to the user
            try {
                val isCrashDetected = keywords.contains("crash_data") || keywords.any { 
                    it.contains("crash_id") || it.contains("rideguard_id") 
                } || crashData != null

                val title = if (isCrashDetected) "🚨 CRASH DETECTED" else "Potential Emergency Detected"
                val body = "From: $sender — ${message.take(160)}"
                
                // Get user ID for the notification
                val userId = getUserId(context) // You might want to get this from somewhere else
                
                NotificationHelper.showEmergencyNotification(
                    context = context,
                    title = title,
                    body = body,
                    isCrashData = isCrashDetected,
                    crashId = crashData?.crashId,
                    rideguardId = crashData?.rideguardId,
                    userId = userId,
                    latitude = crashData?.latitude,
                    longitude = crashData?.longitude
                )
            } catch (ne: Exception) {
                Log.e(TAG, "Failed to show emergency notification", ne)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling potential emergency", e)
        }
    }
    

    

    
    /**
     * Get all SMS messages from device (for testing/debugging)
     */
    fun getAllSmsMessages(context: Context): List<SmsData> {
        val smsList = mutableListOf<SmsData>()
        
        try {
            val cursor = context.contentResolver.query(
                android.provider.Telephony.Sms.CONTENT_URI,
                arrayOf("address", "body", "date", "type"),
                null,
                null,
                "date DESC"
            )
            
            cursor?.use { c ->
                val addressIndex = c.getColumnIndex("address")
                val bodyIndex = c.getColumnIndex("body")
                val dateIndex = c.getColumnIndex("date")
                val typeIndex = c.getColumnIndex("type")
                
                var count = 0
                while (c.moveToNext() && count < 10) { // Limit to 10 for debugging
                    val address = c.getString(addressIndex) ?: "Unknown"
                    val body = c.getString(bodyIndex) ?: ""
                    val date = c.getLong(dateIndex)
                    val type = c.getInt(typeIndex) // 1 = received, 2 = sent
                    smsList.add(SmsData(address, body, date, type))
                    count++
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error reading existing SMS messages", e)
        }
        
        return smsList
    }
    
    /**
     * Enable or disable SMS gateway functionality
     */
    fun setGatewayEnabled(context: Context, enabled: Boolean) {
        isGatewayEnabled = enabled
        
        // Save to SharedPreferences for persistence
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_GATEWAY_ENABLED, enabled).apply()
        

    }
    
    /**
     * Check if SMS gateway is enabled
     */
    fun isGatewayEnabled(context: Context): Boolean {
        // Load from SharedPreferences if not already loaded
        if (!isGatewayEnabled) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            isGatewayEnabled = prefs.getBoolean(KEY_GATEWAY_ENABLED, false)
        }
        return isGatewayEnabled
    }
    
    /**
     * Get gateway status information for debugging
     */
    fun getGatewayStatus(context: Context): String {
        val isEnabled = isGatewayEnabled(context)
        val endpoint = httpService.getEndpointUrl()
        return "Gateway: ${if (isEnabled) "✅ Active" else "❌ Inactive"} | Endpoint: $endpoint"
    }
    
    /**
     * Test the gateway connection (useful for settings screen)
     */
    suspend fun testGatewayConnection(context: Context): Result<String> {
        return if (isGatewayEnabled(context)) {
            try {
                val result = httpService.testServerConnection()
                result.fold(
                    onSuccess = { connected ->
                        val message = if (connected) {
                            "Gateway connection successful!"
                        } else {
                            "Gateway reachable but server returned error"
                        }
                        Result.success(message)
                    },
                    onFailure = { error ->
                        val message = "Gateway connection failed: ${error.message}"
                        Log.e(TAG, message)
                        Result.failure(error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Gateway test error", e)
                Result.failure(e)
            }
        } else {
            Result.success("Gateway is disabled - no connection test needed")
        }
    }
    
    /**
     * Data class for SMS information
     */
    data class SmsData(
        val address: String,
        val body: String,
        val date: Long,
        val type: Int
    )
}