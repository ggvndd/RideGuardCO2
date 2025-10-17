package com.capstoneco2.rideguard.data

/**
 * Data class representing a crash incident identifier
 * Stored in Firebase Firestore crash_id collection
 * Ensures each crash is processed only once despite potential multiple reports
 */
data class CrashId(
    val crashId: String = "", // Unique crash identifier
    val reporterId: String = "", // RideGuard device that first reported
    val userId: String = "", // User associated with the reporting device
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val reportedAt: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = false, // Track if this crash has been processed
    val processingStartedAt: Long? = null,
    val additionalReports: List<CrashReport> = emptyList() // Additional reports for same crash
)

/**
 * Data class for additional crash reports (spam filtering)
 */
data class CrashReport(
    val reporterId: String = "",
    val reportedAt: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

/**
 * Extension function to convert CrashReport to Map for Firestore
 */
fun CrashReport.toMap(): Map<String, Any> {
    return mapOf(
        "reporterId" to reporterId,
        "reportedAt" to reportedAt,
        "latitude" to latitude,
        "longitude" to longitude
    )
}

/**
 * Extension function to convert CrashId to Map for Firestore
 */
fun CrashId.toMap(): Map<String, Any> {
    return mapOf(
        "crashId" to crashId,
        "reporterId" to reporterId,
        "userId" to userId,
        "latitude" to latitude,
        "longitude" to longitude,
        "reportedAt" to reportedAt,
        "isProcessed" to isProcessed,
        "processingStartedAt" to (processingStartedAt ?: ""),
        "additionalReports" to additionalReports.map { it.toMap() }
    )
}