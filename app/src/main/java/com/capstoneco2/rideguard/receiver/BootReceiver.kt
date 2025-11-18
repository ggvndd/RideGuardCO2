package com.capstoneco2.rideguard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver to handle device boot completion
 * Ensures SMS receiver continues to work after device restart
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.i(TAG, "🔄 Device boot completed - RideGuard SMS monitoring ready")
                Log.i(TAG, "🔄 SMS emergency detection system is now active in background")
                
                // Optional: You could start any background services here if needed
                // For now, SMS receiver is automatically enabled by the system
            }
            
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.i(TAG, "🔄 RideGuard app updated - SMS monitoring restored")
                Log.i(TAG, "🔄 Background SMS processing continues after app update")
            }
            
            else -> {
                Log.d(TAG, "Received intent: ${intent.action}")
            }
        }
    }
}