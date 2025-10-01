package com.capstoneco2.rideguard.service

import android.content.Context
import android.util.Log
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
    
    companion object {
        private const val TAG = "SmsService"
        
        // Keywords that might indicate emergency situations
        private val EMERGENCY_KEYWORDS = listOf(
            "help", "emergency", "urgent", "accident", "danger", "fire", "police", 
            "ambulance", "hospital", "injured", "hurt", "trapped", "stuck", "lost",
            "kidnapped", "robbery", "theft", "attack", "violence", "threat",
            "socorro", "emergencia", "urgente", "accidente", "peligro", "fuego",
            "policia", "ambulancia", "hospital", "herido", "atrapado", "perdido"
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
            
            Log.i(TAG, "╔════════════════════════════════════════════════════════════════")
            Log.i(TAG, "║ SMS MESSAGE INTERCEPTED")
            Log.i(TAG, "╠════════════════════════════════════════════════════════════════")
            Log.i(TAG, "║ Sender: $sender")
            Log.i(TAG, "║ Time: $formattedDate")
            Log.i(TAG, "║ Message Length: ${message.length} characters")
            Log.i(TAG, "║ Content: $message")
            Log.i(TAG, "╚════════════════════════════════════════════════════════════════")
            
            // Check for emergency keywords
            val emergencyKeywordsFound = checkForEmergencyKeywords(message)
            val isEmergency = emergencyKeywordsFound.isNotEmpty()
            
            if (isEmergency) {
                Log.w(TAG, "⚠️ POTENTIAL EMERGENCY SMS DETECTED!")
                Log.w(TAG, "⚠️ Keywords found: ${emergencyKeywordsFound.joinToString(", ")}")
                Log.w(TAG, "⚠️ From: $sender")
                Log.w(TAG, "⚠️ Message: $message")
                
                // Handle emergency situation
                handlePotentialEmergency(context, sender, message, emergencyKeywordsFound)
            }
            
            // Send SMS data to server via HTTP POST
            sendSmsToServer(context, sender, message, timestamp, isEmergency, emergencyKeywordsFound)
            
            // Log additional SMS statistics
            logSmsStatistics(sender, message)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing SMS message", e)
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
        keywords: List<String>
    ) {
        try {
            Log.e(TAG, "🚨 EMERGENCY ALERT SYSTEM TRIGGERED 🚨")
            Log.e(TAG, "🚨 Sender: $sender")
            Log.e(TAG, "🚨 Emergency Keywords: ${keywords.joinToString(", ")}")
            Log.e(TAG, "🚨 Full Message: $message")
            Log.e(TAG, "🚨 Timestamp: ${Date()}")
            Log.e(TAG, "🚨 Action Required: Review this message immediately!")
            
            // TODO: Here you could implement:
            // 1. Save to database as potential emergency
            // 2. Send notification to emergency contacts
            // 3. Trigger automatic location sharing
            // 4. Alert user about potential emergency situation
            
            // For now, just extensive logging for debugging
            println("=== EMERGENCY DETECTION SYSTEM ===")
            println("POTENTIAL EMERGENCY SMS DETECTED!")
            println("From: $sender")
            println("Keywords: ${keywords.joinToString(", ")}")
            println("Message: $message")
            println("===================================")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling potential emergency", e)
        }
    }
    
    /**
     * Log SMS statistics for debugging
     */
    private fun logSmsStatistics(sender: String, message: String) {
        try {
            val wordCount = message.split("\\s+".toRegex()).size
            val hasNumbers = message.any { it.isDigit() }
            val hasSpecialChars = message.any { !it.isLetterOrDigit() && !it.isWhitespace() }
            
            Log.d(TAG, "📊 SMS Statistics:")
            Log.d(TAG, "📊 Word Count: $wordCount")
            Log.d(TAG, "📊 Contains Numbers: $hasNumbers")
            Log.d(TAG, "📊 Contains Special Characters: $hasSpecialChars")
            Log.d(TAG, "📊 Sender Domain: ${extractDomain(sender)}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error logging SMS statistics", e)
        }
    }
    
    /**
     * Extract domain or service name from sender
     */
    private fun extractDomain(sender: String): String {
        return when {
            sender.contains("@") -> sender.substringAfter("@")
            sender.all { it.isDigit() || it == '+' } -> "Phone Number"
            sender.length <= 6 -> "Short Code"
            else -> "Unknown Sender Type"
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
                    
                    Log.d(TAG, "📱 Existing SMS #${count + 1}:")
                    Log.d(TAG, "📱 From/To: $address")
                    Log.d(TAG, "📱 Type: ${if (type == 1) "Received" else "Sent"}")
                    Log.d(TAG, "📱 Date: ${Date(date)}")
                    Log.d(TAG, "📱 Message: ${body.take(100)}${if (body.length > 100) "..." else ""}")
                    Log.d(TAG, "📱 ─────────────────────────────────")
                    
                    count++
                }
                
                Log.i(TAG, "📱 Total SMS messages found: ${c.count}")
                Log.i(TAG, "📱 Showing first $count messages")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error reading existing SMS messages", e)
        }
        
        return smsList
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