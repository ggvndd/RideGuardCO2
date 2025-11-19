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
                // Device boot completed - SMS monitoring ready
            }
            
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                // App updated - SMS monitoring restored
            }
            
            else -> {
                // Other intent received
            }
        }
    }
}