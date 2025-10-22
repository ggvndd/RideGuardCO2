package com.capstoneco2.rideguard.data

/**
 * Data class representing an FCM token for a user's device
 * Multiple FCM token entries can exist per device (one per user account)
 * This allows one device to have multiple user accounts, each with their own FCM token
 */
data class FCMToken(
    val id: String = "", // Auto-generated document ID
    val userId: String = "", // UID of the user who owns this token
    val token: String = "", // The actual FCM registration token
    val deviceId: String = "", // Unique device identifier (same across users on same device)
    val deviceName: String = "", // Human-readable device name (e.g., "Samsung Galaxy S23")
    val userDisplayName: String = "", // Display name of the user (e.g., "John Doe")
    val platform: String = "android", // Platform: "android", "ios", "web"
    val appVersion: String = "", // App version when token was generated
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis(), // When this user last used the app on this device
    val isActive: Boolean = true, // For soft deletion when user is no longer using this device
    val isPrimary: Boolean = false // Indicates if this is the primary user account on this device
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
        "userDisplayName" to userDisplayName,
        "platform" to platform,
        "appVersion" to appVersion,
        "createdAt" to createdAt,
        "lastUpdatedAt" to lastUpdatedAt,
        "lastUsedAt" to lastUsedAt,
        "isActive" to isActive,
        "isPrimary" to isPrimary
    )
}