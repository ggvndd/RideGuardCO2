package com.capstoneco2.rideguard.data

/**
 * Data class representing an emergency contact relationship
 * Stored in Firestore emergency_contacts collection
 */
data class EmergencyContact(
    val id: String = "", // Auto-generated document ID
    val ownerUid: String = "", // UID of user who added this contact
    val contactUsername: String = "", // Username of the emergency contact
    val contactUid: String = "", // UID of the emergency contact user
    val addedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true // For future features like temporary disable
)

/**
 * Data class for displaying emergency contact information
 * Contains resolved user information
 */
data class EmergencyContactInfo(
    val contactId: String = "",
    val username: String = "",
    val uid: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val addedAt: Long = 0L
)

/**
 * Extension function to convert EmergencyContact to Map for Firestore
 */
fun EmergencyContact.toMap(): Map<String, Any> {
    return mapOf(
        "ownerUid" to ownerUid,
        "contactUsername" to contactUsername,
        "contactUid" to contactUid,
        "addedAt" to addedAt,
        "isActive" to isActive
    )
}