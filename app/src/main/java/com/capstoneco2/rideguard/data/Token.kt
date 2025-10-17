package com.capstoneco2.rideguard.data

/**
 * Data class representing a token for user-hardware authentication
 * Stored in Firebase Firestore token collection
 * Allows users to have multiple hardware devices
 */
data class Token(
    val tokenId: String = "", // Unique token identifier
    val userId: String = "", // User UID who owns this token
    val rideGuardId: String = "", // Associated hardware device
    val deviceInfo: String = "", // Device information for identification
    val fcmToken: String? = null, // FCM token for push notifications
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null, // Optional expiration time
    val isActive: Boolean = true
)

/**
 * Extension function to convert Token to Map for Firestore
 */
fun Token.toMap(): Map<String, Any> {
    return mapOf(
        "tokenId" to tokenId,
        "userId" to userId,
        "rideGuardId" to rideGuardId,
        "deviceInfo" to deviceInfo,
        "fcmToken" to (fcmToken ?: ""),
        "createdAt" to createdAt,
        "lastUsedAt" to lastUsedAt,
        "expiresAt" to (expiresAt ?: ""),
        "isActive" to isActive
    )
}