package com.capstoneco2.rideguard.data

/**
 * Data class representing a RideGuard hardware device connection
 * Stored in Firebase Firestore ride guard_id collection
 * Manages the relationship between hardware devices and users
 */
data class RideGuardId(
    val id: String = "", // Hardware device identifier
    val currentUserId: String = "", // Currently connected user UID
    val previousUserId: String? = null, // Previous user for history tracking
    val connectedAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

/**
 * Extension function to convert RideGuardId to Map for Firestore
 */
fun RideGuardId.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "currentUserId" to currentUserId,
        "previousUserId" to (previousUserId ?: ""),
        "connectedAt" to connectedAt,
        "lastActiveAt" to lastActiveAt,
        "isActive" to isActive
    )
}