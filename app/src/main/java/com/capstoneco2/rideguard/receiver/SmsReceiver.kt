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
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            try {
                // Extract SMS messages from the intent
                val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                
                if (smsMessages.isNotEmpty()) {
                    // Process each SMS message
                    for (smsMessage in smsMessages) {
                        processSmsMessage(context, smsMessage)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing SMS intent", e)
            }
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