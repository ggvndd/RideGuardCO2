package com.capstoneco2.rideguard.data

/**
 * Data class representing a complete user profile
 * Stored in Firebase Firestore with additional information beyond Firebase Auth
 */
data class UserProfile(
    val uid: String = "", // Firebase Auth UID
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Extension function to convert UserProfile to Map for Firestore
 */
fun UserProfile.toMap(): Map<String, Any> {
    return mapOf(
        "uid" to uid,
        "username" to username,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "profileImageUrl" to (profileImageUrl ?: ""),
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}