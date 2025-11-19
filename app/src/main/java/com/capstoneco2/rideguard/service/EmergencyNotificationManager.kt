package com.capstoneco2.rideguard.service

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing emergency notification lifecycle
 * Handles dismissing sticky notifications when appropriate
 */
@Singleton
class EmergencyNotificationManager @Inject constructor() {
    
    companion object {
        private const val TAG = "EmergencyNotificationManager"
        private const val EMERGENCY_NOTIFICATION_ID = 1001
        private const val EMERGENCY_CONTACT_NOTIFICATION_ID = 1002
        
        // Store active emergency notification IDs
        private val activeEmergencyNotifications = mutableSetOf<Int>()
    }
    
    /**
     * Register that an emergency notification was shown
     */
    fun registerEmergencyNotification(notificationId: Int) {
        synchronized(activeEmergencyNotifications) {
            activeEmergencyNotifications.add(notificationId)
            // Notification registered
        }
    }
    
    /**
     * Dismiss emergency notification when user enters the app
     */
    fun dismissEmergencyNotificationOnAppEnter(context: Context, crashId: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Dismiss all active emergency notifications
            synchronized(activeEmergencyNotifications) {
                activeEmergencyNotifications.forEach { notificationId ->
                    notificationManager.cancel(notificationId)
                    // Notification dismissed
                }
                
                // Also try dismissing with standard IDs
                notificationManager.cancel(EMERGENCY_NOTIFICATION_ID)
                notificationManager.cancel(EMERGENCY_CONTACT_NOTIFICATION_ID)
                
                // Clear the set
                activeEmergencyNotifications.clear()
            }
            
            // Emergency notifications dismissed
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing emergency notification: ${e.message}")
        }
    }
    
    /**
     * Dismiss emergency notification when emergency services are confirmed
     */
    fun dismissEmergencyNotificationOnConfirmed(context: Context, crashId: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Dismiss all active emergency notifications
            synchronized(activeEmergencyNotifications) {
                activeEmergencyNotifications.forEach { notificationId ->
                    notificationManager.cancel(notificationId)
                    // Notification dismissed after confirmation
                }
                
                // Also try dismissing with standard IDs
                notificationManager.cancel(EMERGENCY_NOTIFICATION_ID)
                notificationManager.cancel(EMERGENCY_CONTACT_NOTIFICATION_ID)
                
                // Clear the set
                activeEmergencyNotifications.clear()
            }
            
            // Notifications dismissed after emergency confirmation
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing emergency notification on confirmation: ${e.message}")
        }
    }
    
    /**
     * Manually dismiss specific notification
     */
    fun dismissNotification(context: Context, notificationId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            
            synchronized(activeEmergencyNotifications) {
                activeEmergencyNotifications.remove(notificationId)
            }
            
            // Notification manually dismissed
        } catch (e: Exception) {
            Log.e(TAG, "Error manually dismissing notification: ${e.message}")
        }
    }
    
    /**
     * Get count of active emergency notifications
     */
    fun getActiveNotificationCount(): Int {
        return synchronized(activeEmergencyNotifications) {
            activeEmergencyNotifications.size
        }
    }
    
    /**
     * Check if there are any active emergency notifications
     */
    fun hasActiveEmergencyNotifications(): Boolean {
        return synchronized(activeEmergencyNotifications) {
            activeEmergencyNotifications.isNotEmpty()
        }
    }
}