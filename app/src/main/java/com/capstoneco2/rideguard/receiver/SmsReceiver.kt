package com.capstoneco2.rideguard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.capstoneco2.rideguard.service.SmsService

/**
 * BroadcastReceiver to listen for incoming SMS messages
 * Automatically triggers when SMS is received if permissions are granted
 */
class SmsReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SmsReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "SMS BroadcastReceiver triggered")
        
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            try {
                // Extract SMS messages from the intent
                val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                
                if (smsMessages.isNotEmpty()) {
                    Log.i(TAG, "Received ${smsMessages.size} SMS message(s)")
                    
                    // Process each SMS message
                    for (smsMessage in smsMessages) {
                        processSmsMessage(context, smsMessage)
                    }
                } else {
                    Log.w(TAG, "No SMS messages found in intent")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing SMS intent", e)
            }
        } else {
            Log.d(TAG, "Received non-SMS intent: ${intent.action}")
        }
    }
    
    /**
     * Process individual SMS message
     */
    private fun processSmsMessage(context: Context, smsMessage: SmsMessage) {
        try {
            val sender = smsMessage.originatingAddress ?: "Unknown"
            val messageBody = smsMessage.messageBody ?: ""
            val timestamp = smsMessage.timestampMillis
            
            Log.i(TAG, "=== SMS RECEIVED ===")
            Log.i(TAG, "From: $sender")
            Log.i(TAG, "Message: $messageBody")
            Log.i(TAG, "Timestamp: $timestamp")
            Log.i(TAG, "==================")
            
            // Pass to SMS service for further processing
            val smsService = SmsService()
            smsService.processSmsMessage(
                context = context,
                sender = sender,
                message = messageBody,
                timestamp = timestamp
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing individual SMS message", e)
        }
    }
}