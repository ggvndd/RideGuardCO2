package com.capstoneco2.rideguard.data

/**
 * Data class representing a hospital location
 * Stored in Firebase Firestore hospital collection
 * Contains hospital information for emergency services
 */
data class Hospital(
    val id: String = "", // Unique hospital identifier
    val name: String = "", // Hospital name
    val latitude: Double = 0.0, // Hospital latitude coordinate
    val longitude: Double = 0.0, // Hospital longitude coordinate
    val address: String? = null, // Optional address
    val phoneNumber: String? = null, // Optional phone number
    val emergencyPhone: String? = null, // Optional emergency phone
    val capacity: Int? = null, // Optional capacity info
    val isActive: Boolean = true, // Hospital operational status
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Extension function to convert Hospital to Map for Firestore
 */
fun Hospital.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "name" to name,
        "latitude" to latitude,
        "longitude" to longitude,
        "address" to (address ?: ""),
        "phoneNumber" to (phoneNumber ?: ""),
        "emergencyPhone" to (emergencyPhone ?: ""),
        "capacity" to (capacity ?: 0),
        "isActive" to isActive,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}