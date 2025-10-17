package com.capstoneco2.rideguard.data

/**
 * Data class representing an emergency contact embedded in user profile
 */
data class EmergencyContactRelation(
    val contactUsername: String = "",
    val contactUid: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

/**
 * Data class representing a complete user profile
 * Stored in Firebase Firestore with additional information beyond Firebase Auth
 * Now includes emergency contacts as nested relations
 */
data class UserProfile(
    val uid: String = "", // Firebase Auth UID
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String? = null,
    val emergencyContacts: List<EmergencyContactRelation> = emptyList(), // Nested emergency contacts
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Extension function to convert EmergencyContactRelation to Map for Firestore
 */
fun EmergencyContactRelation.toMap(): Map<String, Any> {
    return mapOf(
        "contactUsername" to contactUsername,
        "contactUid" to contactUid,
        "addedAt" to addedAt,
        "isActive" to isActive
    )
}

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
        "emergencyContacts" to emergencyContacts.map { it.toMap() },
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}