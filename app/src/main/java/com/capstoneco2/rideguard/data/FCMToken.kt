package com.capstoneco2.rideguard.data

/**
 * Data class representing an FCM token for a user's device
 * Multiple tokens can exist per user (multiple devices)
 */
data class FCMToken(
    val id: String = "", // Auto-generated document ID
    val userId: String = "", // UID of the user who owns this token
    val token: String = "", // The actual FCM registration token
    val deviceId: String = "", // Unique device identifier
    val deviceName: String = "", // Human-readable device name (e.g., "John's Phone")
    val platform: String = "android", // Platform: "android", "ios", "web"
    val appVersion: String = "", // App version when token was generated
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true // For soft deletion when device is no longer used
)

/**
 * Extension function to convert FCMToken to Map for Firestore
 */
fun FCMToken.toMap(): Map<String, Any> {
    return mapOf(
        "userId" to userId,
        "token" to token,
        "deviceId" to deviceId,
        "deviceName" to deviceName,
        "platform" to platform,
        "appVersion" to appVersion,
        "createdAt" to createdAt,
        "lastUpdatedAt" to lastUpdatedAt,
        "isActive" to isActive
    )
}