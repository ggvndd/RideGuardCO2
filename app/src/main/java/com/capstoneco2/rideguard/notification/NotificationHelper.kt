package com.capstoneco2.rideguard.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.capstoneco2.rideguard.MainActivity
import com.capstoneco2.rideguard.R
import com.capstoneco2.rideguard.service.EmergencyNotificationManager

object NotificationHelper {
    const val CHANNEL_ID_EMERGENCY = "rideguard_emergency_channel"
    const val CHANNEL_NAME_EMERGENCY = "Emergency Alerts"
    const val CHANNEL_DESC_EMERGENCY = "High priority alerts for detected emergencies"

    const val CHANNEL_ID_INFO = "rideguard_info_channel"
    const val CHANNEL_NAME_INFO = "App Notifications"
    const val CHANNEL_DESC_INFO = "General app notifications and updates"

    private const val EMERGENCY_NOTIFICATION_ID = 1001
    private val emergencyNotificationManager = EmergencyNotificationManager()

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Emergency channel (high importance, sound, vibration)
        val emergencyChannel = NotificationChannel(
            CHANNEL_ID_EMERGENCY,
            CHANNEL_NAME_EMERGENCY,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC_EMERGENCY
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)

            // Try to use bundled raw notification sound if available, else use default
            val soundResId = try {
                context.resources.getIdentifier("notification_sound", "raw", context.packageName)
            } catch (e: Exception) {
                0
            }

            if (soundResId != 0) {
                setSound(
                    Uri.parse("android.resource://${context.packageName}/$soundResId"),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build()
                )
            }
        }

        // Info channel (default importance, no sound)
        val infoChannel = NotificationChannel(
            CHANNEL_ID_INFO,
            CHANNEL_NAME_INFO,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESC_INFO
            enableLights(false)
            enableVibration(false)
        }

        manager.createNotificationChannel(emergencyChannel)
        manager.createNotificationChannel(infoChannel)
    }

    fun showEmergencyNotification(
        context: Context, 
        title: String, 
        body: String, 
        isCrashData: Boolean = false,
        crashId: String? = null,
        rideguardId: String? = null,
        userId: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "Blackbox")
            if (isCrashData) {
                putExtra("emergency_type", "crash")
                putExtra("crash_id", crashId)
                putExtra("rideguard_id", rideguardId)
                putExtra("user_id", userId)
                putExtra("latitude", latitude ?: 0.0)
                putExtra("longitude", longitude ?: 0.0)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_EMERGENCY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        // For crash data: make persistent (sticky) and use red accent
        if (isCrashData) {
            builder
                .setOngoing(true)  // Makes notification persistent/sticky
                .setAutoCancel(false)  // Prevent swipe-to-dismiss
                .setColor(Color.RED)  // Red color accent like TrafficAccidentDialog
                .setColorized(true)   // Apply color to notification background
                .setDefaults(NotificationCompat.DEFAULT_ALL)  // Sound, vibration, lights
        } else {
            builder.setAutoCancel(true)  // Regular notifications can be dismissed
        }

        with(NotificationManagerCompat.from(context)) {
            notify(EMERGENCY_NOTIFICATION_ID, builder.build())
        }
        
        // Register emergency notification if it's crash data
        if (isCrashData) {
            emergencyNotificationManager.registerEmergencyNotification(EMERGENCY_NOTIFICATION_ID)
        }
    }

    fun showEmergencyContactNotification(
        context: Context,
        crashVictimName: String,
        latitude: Double,
        longitude: Double,
        crashId: String = "TEST_EC",
        rideguardId: String = "EMERGENCY_CONTACT"
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "Blackbox")
            putExtra("emergency_type", "crash")
            putExtra("user_role", "emergency_contact")  // KEY: This sets emergency contact role
            putExtra("crash_victim_name", crashVictimName)
            putExtra("crash_id", crashId)
            putExtra("rideguard_id", rideguardId)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1, // Different request code from regular emergency notification
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🚨 EMERGENCY CONTACT ALERT"
        val body = "Your emergency contact $crashVictimName has been in a traffic accident. Location: $latitude, $longitude. Tap to help them get emergency assistance."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_EMERGENCY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setOngoing(true)  // Makes notification persistent/sticky
            .setAutoCancel(false)  // Prevent swipe-to-dismiss
            .setColor(Color.parseColor("#FF6600"))  // Orange color for emergency contact (different from crash victim red)
            .setColorized(true)   // Apply color to notification background
            .setDefaults(NotificationCompat.DEFAULT_ALL)  // Sound, vibration, lights

        with(NotificationManagerCompat.from(context)) {
            notify(EMERGENCY_NOTIFICATION_ID + 1, builder.build()) // Different notification ID
        }
        
        // Register emergency contact notification
        emergencyNotificationManager.registerEmergencyNotification(EMERGENCY_NOTIFICATION_ID + 1)
    }
}
