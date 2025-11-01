package com.capstoneco2.rideguard.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.capstoneco2.rideguard.MainActivity
import com.capstoneco2.rideguard.R
import com.capstoneco2.rideguard.service.EmergencyNotificationManager

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "MyFCMService"
        const val EMERGENCY_CHANNEL_ID = "rideguard_emergency_channel"
        const val INFO_CHANNEL_ID = "rideguard_info_channel"
    }
    
    private val emergencyNotificationManager = EmergencyNotificationManager()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "📱 FCM Message received from: ${remoteMessage.from}")

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "📊 Message data payload: ${remoteMessage.data}")
            
            // Log each data field individually for debugging
            remoteMessage.data.forEach { (key, value) ->
                Log.d(TAG, "📊   $key = $value")
            }
            
            val title = remoteMessage.data["title"] ?: "RideGuard Alert"
            val body = remoteMessage.data["body"] ?: "New notification"
            
            // Check if this is an emergency notification based on data
            val isEmergency = remoteMessage.data["emergency_type"] == "crash" || 
                             remoteMessage.data["user_role"] == "emergency_contact"
            
            if (isEmergency) {
                handleEmergencyNotification(remoteMessage.data, title, body)
            } else {
                showNotification(title, body, INFO_CHANNEL_ID)
            }
        }

        // Check if message contains a notification payload (fallback)
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Body: ${notification.body}")
            showNotification(
                notification.title ?: "RideGuard",
                notification.body ?: "New notification",
                INFO_CHANNEL_ID
            )
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        
        // Send the new token to your server
        sendRegistrationTokenToServer(token)
    }

    private fun handleEmergencyNotification(data: Map<String, String>, title: String, body: String) {
        Log.d(TAG, "Handling emergency notification")
        
        // Create intent with emergency data
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // Pass emergency data from FCM to MainActivity
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
            
            // Add specific extras for navigation
            putExtra("navigate_to", "Blackbox")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Determine notification style based on user role
        val isEmergencyContact = data["user_role"] == "emergency_contact"
        val notificationColor = if (isEmergencyContact) {
            android.graphics.Color.parseColor("#FF6600") // Orange for emergency contact
        } else {
            android.graphics.Color.RED // Red for crash victim
        }

        createNotificationChannels()
        
        val builder = NotificationCompat.Builder(this, EMERGENCY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setOngoing(true)  // Makes notification persistent/sticky
            .setAutoCancel(false)  // Prevent swipe-to-dismiss
            .setColor(notificationColor)  // Color based on role
            .setColorized(true)   // Apply color to notification background
            .setDefaults(NotificationCompat.DEFAULT_ALL)  // Sound, vibration, lights

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, builder.build())
        
        // Register this as an active emergency notification
        emergencyNotificationManager.registerEmergencyNotification(notificationId)
    }

    private fun showNotification(title: String, message: String, channelId: String) {
        createNotificationChannels()
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Emergency channel (high importance, sound, vibration)
            val emergencyChannel = NotificationChannel(
                EMERGENCY_CHANNEL_ID,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alerts for detected emergencies"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build()
                )
            }

            // Info channel (default importance)
            val infoChannel = NotificationChannel(
                INFO_CHANNEL_ID,
                "App Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications and updates"
                enableLights(false)
                enableVibration(false)
            }

            notificationManager.createNotificationChannel(emergencyChannel)
            notificationManager.createNotificationChannel(infoChannel)
        }
    }

    private fun sendRegistrationTokenToServer(token: String) {
        // TODO: Send token to your backend server
        Log.d(TAG, "Sending registration token to server: ${token.take(20)}...")
        
        // You can implement this to automatically update the FCM token in your backend
        // For now, the MainActivity handles this when the user logs in
    }
}