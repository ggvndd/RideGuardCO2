package com.capstoneco2.rideguard.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing crash incidents with duplicate filtering
 * Ensures each crash is processed only once despite multiple reports
 */
@Singleton
class CrashIdService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("crash_id")

    /**
     * Report a crash incident with duplicate filtering
     * Returns true if this is a new crash, false if it's a duplicate
     */
    suspend fun reportCrash(
        crashId: String,
        reporterId: String,
        userId: String,
        latitude: Double,
        longitude: Double
    ): Result<Boolean> {
        return try {
            val existingCrash = getCrashById(crashId)
            
            if (existingCrash != null) {
                // Crash already exists, add as additional report
                val additionalReport = CrashReport(
                    reporterId = reporterId,
                    reportedAt = System.currentTimeMillis(),
                    latitude = latitude,
                    longitude = longitude
                )
                
                collection.document(crashId)
                    .update("additionalReports", FieldValue.arrayUnion(additionalReport.toMap()))
                    .await()
                
                // Return false indicating this is a duplicate
                Result.success(false)
            } else {
                // New crash, create initial record
                val newCrash = CrashId(
                    crashId = crashId,
                    reporterId = reporterId,
                    userId = userId,
                    latitude = latitude,
                    longitude = longitude,
                    reportedAt = System.currentTimeMillis(),
                    isProcessed = false
                )
                
                collection.document(crashId)
                    .set(newCrash.toMap())
                    .await()
                
                // Return true indicating this is a new crash
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get crash details by crash ID
     */
    suspend fun getCrashById(crashId: String): CrashId? {
        return try {
            val document = collection.document(crashId).get().await()
            if (document.exists()) {
                document.toObject(CrashId::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Mark a crash as being processed (to prevent duplicate processing)
     */
    suspend fun markCrashAsProcessing(crashId: String): Result<Boolean> {
        return try {
            val crashDoc = collection.document(crashId)
            val crash = crashDoc.get().await().toObject(CrashId::class.java)
            
            if (crash != null && !crash.isProcessed) {
                // Use transaction to ensure atomicity
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(crashDoc)
                    val currentCrash = snapshot.toObject(CrashId::class.java)
                    
                    if (currentCrash != null && !currentCrash.isProcessed) {
                        transaction.update(crashDoc, mapOf(
                            "isProcessed" to true,
                            "processingStartedAt" to System.currentTimeMillis()
                        ))
                        true // Successfully marked for processing
                    } else {
                        false // Already being processed
                    }
                }.await()
                
                Result.success(true)
            } else {
                Result.success(false) // Already processed
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all unprocessed crashes
     */
    suspend fun getUnprocessedCrashes(): List<CrashId> {
        return try {
            val query = collection
                .whereEqualTo("isProcessed", false)
                .orderBy("reportedAt")
                .get()
                .await()
            
            query.documents.mapNotNull { document ->
                document.toObject(CrashId::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get crashes reported by a specific user
     */
    suspend fun getCrashesForUser(userId: String): List<CrashId> {
        return try {
            val query = collection
                .whereEqualTo("userId", userId)
                .orderBy("reportedAt")
                .get()
                .await()
            
            query.documents.mapNotNull { document ->
                document.toObject(CrashId::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get crash statistics (for monitoring spam/duplicates)
     */
    suspend fun getCrashStats(crashId: String): Map<String, Any>? {
        return try {
            val crash = getCrashById(crashId)
            if (crash != null) {
                mapOf(
                    "totalReports" to (1 + crash.additionalReports.size),
                    "uniqueReporters" to (listOf(crash.reporterId) + crash.additionalReports.map { it.reporterId }).distinct().size,
                    "firstReportTime" to crash.reportedAt,
                    "lastReportTime" to (crash.additionalReports.maxOfOrNull { it.reportedAt } ?: crash.reportedAt),
                    "isProcessed" to crash.isProcessed
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}